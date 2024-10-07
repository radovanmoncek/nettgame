package server.game.docker.server.net.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.dto.*;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;
import server.game.docker.server.GameServer;

import java.util.*;

public class LobbyPDUInboundHandler implements PDUInboundHandler {
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
    public void handle(PDU p) {
        switch (p.getPDUType()){
            case CREATELOBBYREQ -> createLobby(p);
            case JOINLOBBYREQ -> joinLobby(p);
            case LEAVELOBBYREQ -> leaveLobby(p);
        }
    }

    private void createLobby(PDU p){
        //Client is already connected to a lobby or is in a GameSession
        if (
                lobbyDomain.keySet().stream().map(managedClients::find).map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                        ||
                        sessionDomain.keySet().stream().map(managedClients::find).map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
        )
            return;
        Channel lobbyUnassignClientChannel = unassignedDomain.stream().map(managedClients::find).filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
        Long lobbyID = gameServer.getNextLobbyID(); //todo: Redis?
        //Client is already assigned to a lobby
        if (lobbyUnassignClientChannel == null)
            return;
        lobbyLookup.put(lobbyID, new Lobby((byte) 2, lobbyUnassignClientChannel.id()));
        unassignedDomain.remove(lobbyUnassignClientChannel.id());
        lobbyDomain.put(lobbyUnassignClientChannel.id(), lobbyID);
        System.out.printf("Client %s has created a lobby %d\n", gameServer.transformChID(lobbyUnassignClientChannel.id()), lobbyID);

        PDU outPDU = new PDU();
        CreateLobbyRes res = new CreateLobbyRes();
        outPDU.setPDUType(PDUType.CREATELOBBYRES);
        outPDU.setAddress(lobbyUnassignClientChannel.remoteAddress());
        res.setLobbyId(lobbyID);
        outPDU.setData(res);
        gameServer.sendUnicast(outPDU);
        refreshLobbyList();
    }

    private void joinLobby(PDU p) {
        JoinLobbyReq joinLobbyReq = (JoinLobbyReq) p.getData();
        if (lobbyLookup.get(joinLobbyReq.getLobbyID()) == null || lobbyLookup.get(joinLobbyReq.getLobbyID()).isFull())
            return;
        Channel unassignLobbyCh = unassignedDomain.stream().map(managedClients::find).filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
        if (unassignLobbyCh == null) {
            unassignLobbyCh = lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
            if (unassignLobbyCh == null || lobbyLookup.get(joinLobbyReq.getLobbyID()).getClientIDs().contains(unassignLobbyCh.id()))
                return;
            lobbyLookup.get(lobbyDomain.remove(unassignLobbyCh.id())).leave(unassignLobbyCh.id());
        } else
            unassignedDomain.remove(unassignLobbyCh.id());
        lobbyDomain.put(unassignLobbyCh.id(), joinLobbyReq.getLobbyID());
        lobbyLookup.get(joinLobbyReq.getLobbyID()).join(unassignLobbyCh.id());
        JoinLobbyRes joinLobbyRes = new JoinLobbyRes();
        joinLobbyRes.setLobbyID(joinLobbyReq.getLobbyID());
        PDU joinResPDU = new PDU();
        joinResPDU.setPDUType(PDUType.JOINLOBBYRES);
        joinResPDU.setAddress(unassignLobbyCh.remoteAddress());
        joinResPDU.setData(joinLobbyRes);
        gameServer.sendUnicast(joinResPDU);
        JoinLobbyRes justJoinInfo = new JoinLobbyRes();
        justJoinInfo.setLobbyID(-1L);

        PDU jJIPDU = new PDU();
        jJIPDU.setPDUType(PDUType.JOINLOBBYRES);
        jJIPDU.setAddress(unassignLobbyCh.remoteAddress());
        jJIPDU.setData(justJoinInfo);
        gameServer.sendMulticastLobby(jJIPDU);
        //todo: send to lobbyDomain Multicast to notify other lobby members
        refreshLobbyList();
    }

    private void leaveLobby(PDU p) {
        Channel assignedLobbyChannel = lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
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
                LeaveLobbyRes leaveLobbyRes = new LeaveLobbyRes();
                leaveLobbyRes.setLeader(false);
                pLeave.setData(leaveLobbyRes);
                gameServer.sendUnicast(pLeave);
            }
        } else {
            lobbyID = lobbyDomain.remove(assignedLobbyChannel.id());
            unassignedDomain.add(assignedLobbyChannel.id());
            lobby.leave(assignedLobbyChannel.id());

            Channel lobbyLeadCh = gameServer.findDomainChannel(lobby.getLeaderID()).orElse(null);
            if (lobbyLeadCh == null) {
                System.err.printf("Could not find lobby %d leader %d and send disconnect info", lobbyID, gameServer.transformChID(lobby.getLeaderID()));
            } else {
                LeaveLobbyRes justLeaveInfo = new LeaveLobbyRes();
                justLeaveInfo.setLeader(true);

                PDU lobbyLeaveLeaderInfo = new PDU();
                lobbyLeaveLeaderInfo.setPDUType(PDUType.LEAVELOBBYRES);
                lobbyLeaveLeaderInfo.setAddress(lobbyLeadCh.remoteAddress());
                lobbyLeaveLeaderInfo.setData(justLeaveInfo);
                gameServer.sendUnicast(lobbyLeaveLeaderInfo);
            }
        }
        LeaveLobbyRes normalLeave = new LeaveLobbyRes();
        normalLeave.setLeader(false);
        PDU pLeave = new PDU();
        pLeave.setPDUType(PDUType.LEAVELOBBYRES);
        pLeave.setAddress(assignedLobbyChannel.remoteAddress());
        pLeave.setData(normalLeave);
        gameServer.sendUnicast(pLeave);
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
