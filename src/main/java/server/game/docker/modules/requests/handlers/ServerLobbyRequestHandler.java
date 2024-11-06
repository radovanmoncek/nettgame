package server.game.docker.modules.requests.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.modules.beacons.pdus.PDULobbyBeacon;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.modules.updates.pdus.PDULobbyUpdate;
import server.game.docker.GameServer;

import java.util.*;

public class ServerLobbyRequestHandler extends SimpleChannelInboundHandler<PDULobbyReq> {
    private final GameServer gameServer;

    public ServerLobbyRequestHandler(
            GameServer gameServer
    ) {
        this.gameServer = gameServer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, PDULobbyReq lobbyReq) {
        final var channel = channelHandlerContext.channel();
        switch (lobbyReq.getActionFlag()) {
            case 0 -> createLobby(channel);
            case 1 -> joinLobby(lobbyReq, channel);
            case 2 -> leaveLobby(lobbyReq, channel);
            case 3 -> refreshLobbyList(channel);
        }
    }

    private void createLobby(final Channel channel) {
        if (gameServer.isChannelInLobbyDomain(channel) || gameServer.isChannelInSessionDomain(channel))
            return;

        final Channel lobbyUnassignedClientChannel = gameServer.findUnassignedChannel(channel);
        final Long lobbyID = gameServer.getNextLobbyID(); //todo: Redis?

        if (lobbyUnassignedClientChannel == null)
            return;

        gameServer.putLobby(lobbyID, new Lobby((byte) 2, lobbyUnassignedClientChannel.id()));
        gameServer.assignChannelToLobbyDomain(lobbyUnassignedClientChannel.id(), lobbyID);

        System.out.printf("Client: %s | has created a lobby: %d | with members: %s\n", gameServer.transformChID(lobbyUnassignedClientChannel.id()), lobbyID, gameServer.getLobbyMembersTransformed(lobbyID)); //todo: log4j

        final var lobbyUpdate = new PDULobbyUpdate();
        lobbyUpdate.setLobbyId(lobbyID);
        lobbyUpdate.setLeader(true);
        lobbyUpdate.setStateFlag(PDULobbyUpdate.CREATED);
        lobbyUpdate.setMembers(gameServer.getLobbyMembersTransformed(lobbyID));
        gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);
        refreshLobbyList(channel);
    }

    private void joinLobby(PDULobbyReq lobbyReq, Channel channel) {
        if (!gameServer.existsLobbyWithID(lobbyReq.getLobbyID()) || gameServer.isLobbyFull(lobbyReq.getLobbyID()))
            return;
        final Channel unassignedLobbyChannel = gameServer.findUnassignedChannel(channel);
        if (unassignedLobbyChannel == null && !gameServer.assignChannel(channel.id())) {
            final Channel assignedLobbyChannel = gameServer.findLobbyAssignedChannel(channel);

            if (assignedLobbyChannel == null || gameServer.isChannelInLobby(lobbyReq.getLobbyID(), assignedLobbyChannel.id()))
                return;

            Long prevLobbyID = gameServer.unassignLobbyChannel(assignedLobbyChannel.id());

            gameServer.removeChannelFromLobby(prevLobbyID, assignedLobbyChannel.id());

            if(gameServer.isChannelLobbyLeader(prevLobbyID, assignedLobbyChannel.id())) {
                disbandLobby(prevLobbyID, assignedLobbyChannel);
            }

            System.out.printf("Client: %d | previous lobby: %d | desired lobby %d | from already existing lobby\n", gameServer.transformChID(channel.id()), prevLobbyID, lobbyReq.getLobbyID());
        }

        gameServer.assignChannelToLobbyDomain(channel.id(), lobbyReq.getLobbyID());
        gameServer.addChannelToLobby(lobbyReq.getLobbyID(), channel.id());

        PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
        lobbyUpdate.setLobbyId(lobbyReq.getLobbyID());
        lobbyUpdate.setLeader(false);
        lobbyUpdate.setStateFlag(PDULobbyUpdate.JOINED);
        lobbyUpdate.setMembers(gameServer.getLobbyMembersTransformed(lobbyReq.getLobbyID()));
        gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);

        PDULobbyUpdate lobbyUpdateMulti = new PDULobbyUpdate();
        lobbyUpdateMulti.setLobbyId(lobbyReq.getLobbyID());
        lobbyUpdateMulti.setLeader(gameServer.isChannelLobbyLeader(lobbyReq.getLobbyID(), channel.id()));
        lobbyUpdateMulti.setStateFlag(PDULobbyUpdate.MEMBERJOINED);
        lobbyUpdateMulti.setMembers(gameServer.getLobbyMembersTransformed(lobbyReq.getLobbyID()));
        gameServer.sendMulticastLobby(PDUType.LOBBYUPDATE, lobbyUpdateMulti, channel);
        refreshLobbyList(channel);

        System.out.printf("Client: %d | joined lobby %d\n", gameServer.transformChID(channel.id()), lobbyReq.getLobbyID());
    }

    private void leaveLobby(Channel channel) {
        final Channel assignedLobbyChannel = gameServer.findLobbyAssignedChannel(channel);

        if (assignedLobbyChannel == null) {
            return;
        }

        final long lobbyID = gameServer.unassignLobbyChannel(assignedLobbyChannel.id());

        gameServer.removeChannelFromLobby(lobbyID, assignedLobbyChannel.id());

        if (gameServer.isChannelLobbyLeader(lobbyID, channel.id())) {
            disbandLobby(lobbyID, assignedLobbyChannel);
        }
        else {
            final Channel lobbyLeaderChannel = gameServer.findDomainChannel(gameServer.getLobbyLeaderChannel(lobbyID).orElseThrow().id()).orElse(null);

            if (lobbyLeaderChannel == null) {
                System.err.printf("Runt lobby: Could not find lobby: %d leader: %d and send disconnect info\n", lobbyID, gameServer.transformChID(gameServer.getLobbyLeaderChannel(lobbyID).orElseThrow().id()));
            }
            else {
                final PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
                lobbyUpdate.setLeader(true);
                lobbyUpdate.setStateFlag(PDULobbyUpdate.MEMBERLEFT);
                lobbyUpdate.setLobbyId(lobbyID);
                lobbyUpdate.setMembers(gameServer.getLobbyMembersTransformed(lobbyID));

                gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, lobbyLeaderChannel);
            }
        }

        gameServer.addToUnassigned(assignedLobbyChannel.id());

        final PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
        lobbyUpdate.setStateFlag((byte) 2);
        lobbyUpdate.setMembers(List.of());
        lobbyUpdate.setLeader(false);
        lobbyUpdate.setLobbyId(0L);

        gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);
        refreshLobbyList(channel);

        System.out.printf("Client: %d | has left lobby: %d | lobby exists: %b\n", gameServer.transformChID(assignedLobbyChannel.id()), lobbyID, gameServer.existsLobbyWithID(lobbyID));
    }

    private void disbandLobby(Long lobbyID, Channel assignedLobbyChannel) {
        final Collection<ChannelId> otherLobbyMembers = gameServer.getLobbyMembers(lobbyID).stream().filter(c -> !gameServer.isChannelLobbyLeader(lobbyID, c)).toList();

        if (!otherLobbyMembers.isEmpty()) {
            final Channel secondLobbyMemberChannel = gameServer
                    .findDomainChannel(otherLobbyMembers.stream().findAny().orElse(null))
                    .orElse(null);

            if (secondLobbyMemberChannel != null) {
                gameServer.unassignChannel(secondLobbyMemberChannel.id());
                gameServer.addToUnassigned(secondLobbyMemberChannel.id());
                gameServer.removeChannelFromLobby(lobbyID, secondLobbyMemberChannel.id());

                PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
                lobbyUpdate.setStateFlag(PDULobbyUpdate.LEFT);
                gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, secondLobbyMemberChannel);
            }
        }

        gameServer.removeLobby(lobbyID);

        System.out.printf("Lobby: %d disbanded | still exists: %b\n", lobbyID, gameServer.existsLobbyWithID(lobbyID));
    }

    private void refreshLobbyList(Channel contextChannel) {
        if (!gameServer.hasAnyLobbies()) {
            PDULobbyBeacon lobbyBeacon = new PDULobbyBeacon();
            lobbyBeacon.setLobbyListRefresh(true);
            lobbyBeacon.setLobbyID(-1L);
            lobbyBeacon.setLobbyCurOccupancy((byte) -1);
            lobbyBeacon.setLobbyMaxOccupancy((byte) -1);

            gameServer.sendUnicast(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
            gameServer.sendBroadcastUnassigned(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
            gameServer.sendBroadcastLobby(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);

            System.out.println("Sent an empty lobby beacon");
            return;
        }
        boolean refFlag = true;
        for (Long lobbyID : gameServer.accessLobbyLookupIDs()) {
            PDULobbyBeacon lobbyBeacon = new PDULobbyBeacon();
            lobbyBeacon.setLobbyListRefresh(refFlag);
            if (refFlag)
                refFlag = false;
            lobbyBeacon.setLobbyID(lobbyID);
            lobbyBeacon.setLobbyCurOccupancy((byte) gameServer.getLobbyMembers(lobbyID).size());
            lobbyBeacon.setLobbyMaxOccupancy(gameServer.accessMaxOccupancyForLobbyID(lobbyID));

            gameServer.sendUnicast(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
            gameServer.sendBroadcastUnassigned(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
            gameServer.sendBroadcastLobby(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);

            System.out.printf("Sent lobby beacon: %s\n", lobbyBeacon);
        }
    }

    public static class Lobby {
        private final Set<ChannelId> clientIDs;
        private final Byte maxSize;
        private final ChannelId leaderClientID;
        private final List<String> lobbyMessageHistory;

        public Lobby(Byte maxSize, ChannelId leaderClientID) {
            clientIDs = new HashSet<>(List.of(leaderClientID));
            this.maxSize = maxSize;
            this.leaderClientID = leaderClientID;
            //chat exists as only a concept
            lobbyMessageHistory = new LinkedList<>();
        }

        /**
         * Lobbies bigger than the bounds of a Byte {@code Byte.MAX_VALUE} are not expected to be supported
         *
         * @return {@link Byte} the size of this lobby
         */
        public Byte getCurrentSize() {
            return (byte) clientIDs.size();
        }

        public void join(ChannelId clientID) {
            if (isFull())
                return;
            clientIDs.add(clientID);
        }

        public void leave(ChannelId clientID) {
            clientIDs.remove(clientID);
        }

        public ChannelId getLeaderID() {
            return leaderClientID;
        }

        public Byte getMaxSize() {
            return maxSize;
        }

        public Set<ChannelId> getClientIDs() {
            return clientIDs;
        }

        public boolean isFull() {
            return clientIDs.size() == maxSize;
        }

        public void appendLobbyChatMessage(String message) {
            lobbyMessageHistory.add(message);
        }
    }
}
