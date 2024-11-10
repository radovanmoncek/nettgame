package server.game.docker.modules.requests.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.requests.facades.LobbyRequestServerFacade;
import server.game.docker.modules.requests.pdus.LobbyReqPDU;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class LobbyRequestServerHandler extends SimpleChannelInboundHandler<LobbyReqPDU> {
    private final LobbyRequestServerFacade lobbyRequestServerFacade;

    public LobbyRequestServerHandler(
            LobbyRequestServerFacade lobbyRequestServerFacade
    ) {
        this.lobbyRequestServerFacade = lobbyRequestServerFacade;
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, LobbyReqPDU lobbyReq) {
        final var channel = channelHandlerContext.channel();
        switch (lobbyReq.getActionFlag()) {
            case 0 -> createLobby(channel);
            case 1 -> joinLobby(lobbyReq, channel);
            case 2 -> leaveLobby(channel);
            case 3 -> refreshLobbyList(channel);
        }
    }

    private void createLobby(final Channel channel) {
//        if (lobbyRequestServerFacade.isChannelInLobbyDomain(channel) || lobbyRequestServerFacade.isChannelInSessionDomain(channel))
//            return;
//
//        final Channel lobbyUnassignedClientChannel = lobbyRequestServerFacade.findUnassignedChannel(channel);
//        final Long lobbyID = lobbyRequestServerFacade.getNextLobbyID(); //todo: Redis?
//
//        if (lobbyUnassignedClientChannel == null)
//            return;
//
//        lobbyRequestServerFacade.putLobby(lobbyID, new Lobby((byte) 2, lobbyUnassignedClientChannel.id()));
//        lobbyRequestServerFacade.assignChannelToLobbyDomain(lobbyUnassignedClientChannel.id(), lobbyID);
//
//        System.out.printf("Client: %s | has created a lobby: %d | with members: %s\n", lobbyRequestServerFacade.transformChID(lobbyUnassignedClientChannel.id()), lobbyID, lobbyRequestServerFacade.getLobbyMembersTransformed(lobbyID)); //todo: log4j
//
//        final var lobbyUpdate = new PDULobbyUpdate();
//        lobbyUpdate.setLobbyId(lobbyID);
//        lobbyUpdate.setLeader(true);
//        lobbyUpdate.setStateFlag(PDULobbyUpdate.CREATED);
//        lobbyUpdate.setMembers(lobbyRequestServerFacade.getLobbyMembersTransformed(lobbyID));
//        lobbyRequestServerFacade.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);
//        refreshLobbyList(channel);
    }

    private void joinLobby(LobbyReqPDU lobbyReq, Channel channel) {
//        if (!lobbyRequestServerFacade.existsLobbyWithID(lobbyReq.getLobbyID()) || lobbyRequestServerFacade.isLobbyFull(lobbyReq.getLobbyID()))
//            return;
//        final Channel unassignedLobbyChannel = lobbyRequestServerFacade.findUnassignedChannel(channel);
//        if (unassignedLobbyChannel == null && !lobbyRequestServerFacade.assignChannel(channel.id())) {
//            final Channel assignedLobbyChannel = lobbyRequestServerFacade.findLobbyAssignedChannel(channel);
//
//            if (assignedLobbyChannel == null || lobbyRequestServerFacade.isChannelInLobby(lobbyReq.getLobbyID(), assignedLobbyChannel.id()))
//                return;
//
//            Long prevLobbyID = lobbyRequestServerFacade.unassignLobbyChannel(assignedLobbyChannel.id());
//
//            lobbyRequestServerFacade.removeChannelFromLobby(prevLobbyID, assignedLobbyChannel.id());
//
//            if(lobbyRequestServerFacade.isChannelLobbyLeader(prevLobbyID, assignedLobbyChannel.id())) {
//                disbandLobby(prevLobbyID, assignedLobbyChannel);
//            }
//
//            System.out.printf("Client: %d | previous lobby: %d | desired lobby %d | from already existing lobby\n", lobbyRequestServerFacade.transformChID(channel.id()), prevLobbyID, lobbyReq.getLobbyID());
//        }
//
//        lobbyRequestServerFacade.assignChannelToLobbyDomain(channel.id(), lobbyReq.getLobbyID());
//        lobbyRequestServerFacade.addChannelToLobby(lobbyReq.getLobbyID(), channel.id());
//
//        PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
//        lobbyUpdate.setLobbyId(lobbyReq.getLobbyID());
//        lobbyUpdate.setLeader(false);
//        lobbyUpdate.setStateFlag(PDULobbyUpdate.JOINED);
//        lobbyUpdate.setMembers(lobbyRequestServerFacade.getLobbyMembersTransformed(lobbyReq.getLobbyID()));
//        lobbyRequestServerFacade.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);
//
//        PDULobbyUpdate lobbyUpdateMulti = new PDULobbyUpdate();
//        lobbyUpdateMulti.setLobbyId(lobbyReq.getLobbyID());
//        lobbyUpdateMulti.setLeader(lobbyRequestServerFacade.isChannelLobbyLeader(lobbyReq.getLobbyID(), channel.id()));
//        lobbyUpdateMulti.setStateFlag(PDULobbyUpdate.MEMBERJOINED);
//        lobbyUpdateMulti.setMembers(lobbyRequestServerFacade.getLobbyMembersTransformed(lobbyReq.getLobbyID()));
//        lobbyRequestServerFacade.sendMulticastLobby(PDUType.LOBBYUPDATE, lobbyUpdateMulti, channel);
//        refreshLobbyList(channel);
//
//        System.out.printf("Client: %d | joined lobby %d\n", lobbyRequestServerFacade.transformChID(channel.id()), lobbyReq.getLobbyID());
    }

    private void leaveLobby(Channel channel) {
//        final Channel assignedLobbyChannel = lobbyRequestServerFacade.findLobbyAssignedChannel(channel);
//
//        if (assignedLobbyChannel == null) {
//            return;
//        }
//
//        final long lobbyID = lobbyRequestServerFacade.unassignLobbyChannel(assignedLobbyChannel.id());
//
//        lobbyRequestServerFacade.removeChannelFromLobby(lobbyID, assignedLobbyChannel.id());
//
//        if (lobbyRequestServerFacade.isChannelLobbyLeader(lobbyID, channel.id())) {
//            disbandLobby(lobbyID, assignedLobbyChannel);
//        }
//        else {
//            final Channel lobbyLeaderChannel = lobbyRequestServerFacade.findDomainChannel(lobbyRequestServerFacade.getLobbyLeaderChannel(lobbyID).orElseThrow().id()).orElse(null);
//
//            if (lobbyLeaderChannel == null) {
//                System.err.printf("Runt lobby: Could not find lobby: %d leader: %d and send disconnect info\n", lobbyID, lobbyRequestServerFacade.transformChID(lobbyRequestServerFacade.getLobbyLeaderChannel(lobbyID).orElseThrow().id()));
//            }
//            else {
//                final PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
//                lobbyUpdate.setLeader(true);
//                lobbyUpdate.setStateFlag(PDULobbyUpdate.MEMBERLEFT);
//                lobbyUpdate.setLobbyId(lobbyID);
//                lobbyUpdate.setMembers(lobbyRequestServerFacade.getLobbyMembersTransformed(lobbyID));
//
//                lobbyRequestServerFacade.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, lobbyLeaderChannel);
//            }
//        }
//
//        lobbyRequestServerFacade.addToUnassigned(assignedLobbyChannel.id());
//
//        final PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
//        lobbyUpdate.setStateFlag((byte) 2);
//        lobbyUpdate.setMembers(List.of());
//        lobbyUpdate.setLeader(false);
//        lobbyUpdate.setLobbyId(0L);
//
//        lobbyRequestServerFacade.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);
//        refreshLobbyList(channel);
//
//        System.out.printf("Client: %d | has left lobby: %d | lobby exists: %b\n", lobbyRequestServerFacade.transformChID(assignedLobbyChannel.id()), lobbyID, lobbyRequestServerFacade.existsLobbyWithID(lobbyID));
    }

    private void disbandLobby(Long lobbyID, Channel assignedLobbyChannel) {
//        final Collection<ChannelId> otherLobbyMembers = lobbyRequestServerFacade.getLobbyMembers(lobbyID).stream().filter(c -> !lobbyRequestServerFacade.isChannelLobbyLeader(lobbyID, c)).toList();
//
//        if (!otherLobbyMembers.isEmpty()) {
//            final Channel secondLobbyMemberChannel = lobbyRequestServerFacade
//                    .findDomainChannel(otherLobbyMembers.stream().findAny().orElse(null))
//                    .orElse(null);
//
//            if (secondLobbyMemberChannel != null) {
//                lobbyRequestServerFacade.unassignChannel(secondLobbyMemberChannel.id());
//                lobbyRequestServerFacade.addToUnassigned(secondLobbyMemberChannel.id());
//                lobbyRequestServerFacade.removeChannelFromLobby(lobbyID, secondLobbyMemberChannel.id());
//
//                PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
//                lobbyUpdate.setStateFlag(PDULobbyUpdate.LEFT);
//                lobbyRequestServerFacade.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, secondLobbyMemberChannel);
//            }
//        }
//
//        lobbyRequestServerFacade.removeLobby(lobbyID);
//
//        System.out.printf("Lobby: %d disbanded | still exists: %b\n", lobbyID, lobbyRequestServerFacade.existsLobbyWithID(lobbyID));
    }

    private void refreshLobbyList(Channel contextChannel) {
//        if (!lobbyRequestServerFacade.hasAnyLobbies()) {
//            PDULobbyBeacon lobbyBeacon = new PDULobbyBeacon();
//            lobbyBeacon.setLobbyListRefresh(true);
//            lobbyBeacon.setLobbyID(-1L);
//            lobbyBeacon.setLobbyCurOccupancy((byte) -1);
//            lobbyBeacon.setLobbyMaxOccupancy((byte) -1);
//
//            lobbyRequestServerFacade.sendUnicast(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
//            lobbyRequestServerFacade.sendBroadcastUnassigned(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
//            lobbyRequestServerFacade.sendBroadcastLobby(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
//
//            System.out.println("Sent an empty lobby beacon");
//            return;
//        }
//        boolean refFlag = true;
//        for (Long lobbyID : lobbyRequestServerFacade.accessLobbyLookupIDs()) {
//            PDULobbyBeacon lobbyBeacon = new PDULobbyBeacon();
//            lobbyBeacon.setLobbyListRefresh(refFlag);
//            if (refFlag)
//                refFlag = false;
//            lobbyBeacon.setLobbyID(lobbyID);
//            lobbyBeacon.setLobbyCurOccupancy((byte) lobbyRequestServerFacade.getLobbyMembers(lobbyID).size());
//            lobbyBeacon.setLobbyMaxOccupancy(lobbyRequestServerFacade.accessMaxOccupancyForLobbyID(lobbyID));
//
//            lobbyRequestServerFacade.sendUnicast(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
//            lobbyRequestServerFacade.sendBroadcastUnassigned(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
//            lobbyRequestServerFacade.sendBroadcastLobby(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
//
//            System.out.printf("Sent lobby beacon: %s\n", lobbyBeacon);
//        }
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
