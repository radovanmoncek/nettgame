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
        refreshLobbyList();
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
        refreshLobbyList();
    }

    private void leaveLobby(LobbyReq in, Channel channel) {
        Channel assignedLobbyChannel = lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> in.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
        if (assignedLobbyChannel == null)
            return;
        Lobby lobby = lobbyLookup.get(lobbyDomain.get(assignedLobbyChannel.id()));
        Long lobbyID;
        if (lobby.getLeaderID().compareTo(assignedLobbyChannel.id()) == 0) {
            ChannelId secondLobbyMember = lobbyLookup.remove(lobbyID = lobbyDomain.remove(assignedLobbyChannel.id())).getClientIDs().stream().filter(c -> !c.equals(lobby.getLeaderID())).findAny().orElse(null);
            Channel sLMC = lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> c.id().equals(secondLobbyMember)).findAny().orElse(null);
            unassignedDomain.add(assignedLobbyChannel.id());
            if (sLMC != null) {
                lobbyDomain.remove(sLMC.id());
                unassignedDomain.add(sLMC.id());
                lobby.leave(secondLobbyMember);
                PDU pLeave = new PDU();
                pLeave.setPDUType(PDUType.LEAVELOBBYRES);
                pLeave.setAddress(sLMC.remoteAddress());
                LobbyUpdate.LeaveLobbyRes leaveLobbyRes = new LobbyUpdate.LeaveLobbyRes();
                leaveLobbyRes.setLeader(false);
                pLeave.setData(leaveLobbyRes);
                gameServer.sendUnicast(PDUType.LOBBYUPDATE, pLeave);
            }
        } else {
            lobbyID = lobbyDomain.remove(assignedLobbyChannel.id());
            unassignedDomain.add(assignedLobbyChannel.id());
            lobby.leave(assignedLobbyChannel.id());

            Channel lobbyLeadCh = gameServer.findDomainChannel(lobby.getLeaderID()).orElse(null);
            if (lobbyLeadCh == null) {
                System.err.printf("Could not find lobby %d leader %d and send disconnect info", lobbyID, gameServer.transformChID(lobby.getLeaderID()));
            } else {
                LobbyUpdate.LeaveLobbyRes justLeaveInfo = new LobbyUpdate.LeaveLobbyRes();
                justLeaveInfo.setLeader(true);

                PDU lobbyLeaveLeaderInfo = new PDU();
                lobbyLeaveLeaderInfo.setPDUType(PDUType.LEAVELOBBYRES);
                lobbyLeaveLeaderInfo.setAddress(lobbyLeadCh.remoteAddress());
                lobbyLeaveLeaderInfo.setData(justLeaveInfo);
                gameServer.sendUnicast(PDUType.LOBBYUPDATE, lobbyLeaveLeaderInfo);
            }
        }
        LobbyUpdate.LeaveLobbyRes normalLeave = new LobbyUpdate.LeaveLobbyRes();
        normalLeave.setLeader(false);
        PDU pLeave = new PDU();
        pLeave.setPDUType(PDUType.LEAVELOBBYRES);
        pLeave.setAddress(assignedLobbyChannel.remoteAddress());
        pLeave.setData(normalLeave);
        gameServer.sendUnicast(PDUType.LOBBYUPDATE, pLeave);
        System.out.printf("Client %d has left lobby %d\n", gameServer.transformChID(assignedLobbyChannel.id()), lobbyID);
        refreshLobbyList();
    }

    private void refreshLobbyList() {
        if (lobbyLookup.isEmpty()) {
            LobbyBeacon beacon = new LobbyBeacon();
            beacon.setLobbyListRefresh(true);
            beacon.setLobbyID(-1L);
            beacon.setLobbyCurOccupancy((byte) -1);
            beacon.setLobbyMaxOccupancy((byte) -1);
            PDU outherBOPDU = new PDU();
            outherBOPDU.setPDUType(PDUType.LOBBYBEACON);
            outherBOPDU.setData(beacon);
            gameServer.sendBroadcastUnassigned(outherBOPDU);
            gameServer.sendBroadcastLobby(outherBOPDU);
            return;
        }
        boolean refFlag = true;
        for (Map.Entry<Long, Lobby> e : lobbyLookup.entrySet()) {
            LobbyBeacon beacon = new LobbyBeacon();
            beacon.setLobbyListRefresh(refFlag);
            if (refFlag)
                refFlag = false;
            beacon.setLobbyID(e.getKey());
            beacon.setLobbyCurOccupancy(e.getValue().getCurrentSize());
            beacon.setLobbyMaxOccupancy(e.getValue().getMaxSize());
            PDU outherBOPDU = new PDU();
            outherBOPDU.setPDUType(PDUType.LOBBYBEACON);
            outherBOPDU.setData(beacon);
            gameServer.sendBroadcastUnassigned(outherBOPDU);
            gameServer.sendBroadcastLobby(outherBOPDU);
        }
    }

    public static class Lobby {
        private final Set<ChannelId> clientIDs;
        private final Byte maxSize;
        private final ChannelId leaderClientID;

        public Lobby(Byte maxSize, ChannelId leaderClientID) {
            clientIDs = new HashSet<>(List.of(leaderClientID));
            this.maxSize = maxSize;
            this.leaderClientID = leaderClientID;
//      chat exists as only a concept
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

        public void appendLobbyChatMessage(String message) {

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

        public static class LobbyChat {
            private final List<String> lobbyMessageHistory;

            public LobbyChat() {
                lobbyMessageHistory = new LinkedList<>();
            }

            public void appendMessage(String message) {
                lobbyMessageHistory.add(message);
            }
        }
    }
}
