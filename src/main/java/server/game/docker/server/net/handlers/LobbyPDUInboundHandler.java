package server.game.docker.server.net.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.pdus.LobbyBeacon;
import server.game.docker.net.modules.pdus.LobbyReq;
import server.game.docker.net.modules.pdus.LobbyUpdate;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.server.GameServer;

import java.util.*;

public class LobbyPDUInboundHandler extends PDUInboundHandler {
    private final GameServer gameServer;
    private final Map<Long, Lobby> lobbyLookup;
    private final Set<ChannelId> unassignedDomain;
    private final ChannelGroup managedClients;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<ChannelId, Long> sessionDomain;

    public LobbyPDUInboundHandler(
            GameServer gameServer,
            Map<Long, Lobby> lobbyLookup,
            Set<ChannelId> unassignedDomain,
            ChannelGroup managedClients,
            Map<ChannelId, Long> lobbyDomain,
            Map<ChannelId, Long> sessionDomain
    ) {
        this.gameServer = gameServer;
        this.lobbyLookup = lobbyLookup;
        this.unassignedDomain = unassignedDomain;
        this.managedClients = managedClients;
        this.lobbyDomain = lobbyDomain;
        this.sessionDomain = sessionDomain;
    }

    @Override
    public void handle(PDU pdu) {
    }

    @Override
    public void handle(PDU in, Channel channel) {
        LobbyReq lobbyReq = (LobbyReq) in;
        switch (lobbyReq.getActionFlag()){
            case 0 -> createLobby(lobbyReq, channel);
            case 1 -> joinLobby(lobbyReq, channel);
            case 2 -> leaveLobby(lobbyReq, channel);
        }
    }

    private void createLobby(LobbyReq in, Channel channel){
        //Client is already connected to a lobby or is in a GameSession
        if (
                lobbyDomain.keySet().stream().map(managedClients::find).map(Channel::remoteAddress).anyMatch(channel.remoteAddress()::equals)
                        ||
                        sessionDomain.keySet().stream().map(managedClients::find).map(Channel::remoteAddress).anyMatch(channel.remoteAddress()::equals)
        )
            return;
        Channel lobbyUnassignClientChannel = unassignedDomain.stream().map(managedClients::find).filter(c -> channel.remoteAddress().equals(c.remoteAddress())).findAny().orElse(null);
        Long lobbyID = gameServer.getNextLobbyID(); //todo: Redis?
        //Client is already assigned to a lobby
        if (lobbyUnassignClientChannel == null)
            return;
        lobbyLookup.put(lobbyID, new Lobby((byte) 2, lobbyUnassignClientChannel.id()));
        unassignedDomain.remove(lobbyUnassignClientChannel.id());
        lobbyDomain.put(lobbyUnassignClientChannel.id(), lobbyID);
        System.out.printf("Client %s has created a lobby %d\n", gameServer.transformChID(lobbyUnassignClientChannel.id()), lobbyID);

        LobbyUpdate res = new LobbyUpdate();
        res.setLobbyId(lobbyID);
        gameServer.sendUnicast(PDUType.LOBBYUPDATE, res, channel);
        refreshLobbyList(channel);
    }

    private void joinLobby(LobbyReq lobbyReq, Channel channel) {
        if (lobbyLookup.get(lobbyReq.getLobbyID()) == null || lobbyLookup.get(lobbyReq.getLobbyID()).isFull())
            return;
        Channel unassignLobbyCh = unassignedDomain.stream().map(managedClients::find).filter(c -> channel.remoteAddress().equals(c.remoteAddress())).findAny().orElse(null);
        if (unassignLobbyCh == null) {
            unassignLobbyCh = lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> channel.remoteAddress().equals(c.remoteAddress())).findAny().orElse(null);
            if (unassignLobbyCh == null || lobbyLookup.get(lobbyReq.getLobbyID()).getClientIDs().contains(unassignLobbyCh.id()))
                return;
            lobbyLookup.get(lobbyDomain.remove(unassignLobbyCh.id())).leave(unassignLobbyCh.id());
        } else
            unassignedDomain.remove(unassignLobbyCh.id());
        lobbyDomain.put(unassignLobbyCh.id(), lobbyReq.getLobbyID());
        lobbyLookup.get(lobbyReq.getLobbyID()).join(unassignLobbyCh.id());

        Lobby lobby = lobbyLookup.get(lobbyReq.getLobbyID());
        LobbyUpdate lobbyUpdate = new LobbyUpdate();
        lobbyUpdate.setLobbyId(lobbyReq.getLobbyID());
        //Leader cannot join own lobby
        lobbyUpdate.setLeader(false);
        lobbyUpdate.setStateFlag(1);
        gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);

        //send to lobbyDomain Multicast to notify other lobby members
        LobbyUpdate lobbyUpdateMulti = new LobbyUpdate();
        lobbyUpdateMulti.setLobbyId(lobbyReq.getLobbyID());
        lobbyUpdateMulti.setLeader(lobby.leaderClientID.compareTo(channel.id()) == 0);
        lobbyUpdateMulti.setStateFlag(3);
        gameServer.sendMulticastLobby(PDUType.LOBBYUPDATE, lobbyUpdateMulti, channel);
        refreshLobbyList(channel);
    }

    private void leaveLobby(LobbyReq in, Channel channel) {
        Channel assignedLobbyChannel = lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> channel.remoteAddress().equals(c.remoteAddress())).findAny().orElse(null);
        if (assignedLobbyChannel == null)
            return;
        Lobby lobby = lobbyLookup.get(lobbyDomain.get(assignedLobbyChannel.id()));
        Long lobbyID;
        if (lobby.getLeaderID().compareTo(assignedLobbyChannel.id()) == 0) {
            ChannelId secondLobbyMember = lobbyLookup.remove(lobbyID = lobbyDomain.remove(assignedLobbyChannel.id())).getClientIDs().stream().filter(c -> !c.equals(lobby.getLeaderID())).findAny().orElse(null);
            Channel secondLobbyMemberChannel = lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> c.id().equals(secondLobbyMember)).findAny().orElse(null);
            unassignedDomain.add(assignedLobbyChannel.id());
            if (secondLobbyMemberChannel != null) {
                lobbyDomain.remove(secondLobbyMemberChannel.id());
                unassignedDomain.add(secondLobbyMemberChannel.id());
                lobby.leave(secondLobbyMember);
                LobbyUpdate lobbyUpdate = new LobbyUpdate();
                lobbyUpdate.setStateFlag(2);
                gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, secondLobbyMemberChannel);
            }
        } else {
            lobbyID = lobbyDomain.remove(assignedLobbyChannel.id());
            unassignedDomain.add(assignedLobbyChannel.id());
            lobby.leave(assignedLobbyChannel.id());

            Channel lobbyLeaderChannel = gameServer.findDomainChannel(lobby.getLeaderID()).orElse(null);
            if (lobbyLeaderChannel == null) {
                System.err.printf("Could not find lobby %d leader %d and send disconnect info", lobbyID, gameServer.transformChID(lobby.getLeaderID()));
            } else {
                LobbyUpdate lobbyUpdate = new LobbyUpdate();
                lobbyUpdate.setLeader(true);
                lobbyUpdate.setStateFlag(4);
                lobbyUpdate.setLobbyId(lobbyID);
                lobbyUpdate.setMembers(lobby.clientIDs.stream().map(gameServer::transformChID).toList());

                gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, lobbyLeaderChannel);
            }
        }
        LobbyUpdate lobbyUpdate = new LobbyUpdate();
        lobbyUpdate.setStateFlag(2);
        gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyUpdate, channel);
        System.out.printf("Client %d has left lobby %d\n", gameServer.transformChID(assignedLobbyChannel.id()), lobbyID);
        refreshLobbyList(channel);
    }

    private void refreshLobbyList(Channel contextChannel) {
        if (lobbyLookup.isEmpty()) {
            LobbyBeacon lobbyBeacon = new LobbyBeacon();
            lobbyBeacon.setLobbyListRefresh(true);
            lobbyBeacon.setLobbyID(-1L);
            lobbyBeacon.setLobbyCurOccupancy((byte) -1);
            lobbyBeacon.setLobbyMaxOccupancy((byte) -1);
            gameServer.sendBroadcastUnassigned(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
            gameServer.sendBroadcastLobby(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
            return;
        }
        boolean refFlag = true;
        for (Map.Entry<Long, Lobby> e : lobbyLookup.entrySet()) {
            LobbyBeacon lobbyBeacon = new LobbyBeacon();
            lobbyBeacon.setLobbyListRefresh(refFlag);
            if (refFlag)
                refFlag = false;
            lobbyBeacon.setLobbyID(e.getKey());
            lobbyBeacon.setLobbyCurOccupancy(e.getValue().getCurrentSize());
            lobbyBeacon.setLobbyMaxOccupancy(e.getValue().getMaxSize());
            gameServer.sendBroadcastUnassigned(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
            gameServer.sendBroadcastLobby(PDUType.LOBBYBEACON, lobbyBeacon, contextChannel);
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
