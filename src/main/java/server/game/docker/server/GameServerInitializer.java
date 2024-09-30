package server.game.docker.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.PDUHandlerEncoder;
import server.game.docker.net.PDUHandlerDecoder;
import server.game.docker.net.PDUInboundHandler;
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.dto.*;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;
import server.game.docker.server.matchmaking.Lobby;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GameServerInitializer {
    private Long lobbyIDAutoIncrement = 1L;
    private final Map<PDUType, LocalPipeline> localPDUPipelines;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<ChannelId, Long> sessionDomain;
    private final Set<ChannelId> unassignedDomain;
    private final Map<Long, Lobby> lobbyLookup;
    private final Map<ChannelId, Long> channelIDClientIDLookup;
    private final ChannelGroup managedClients;

    public GameServerInitializer(
            Map<PDUType, LocalPipeline> localPDUPipelines,
            Map<ChannelId, Long> lobbyDomain,
            Map<ChannelId, Long> sessionDomain,
            Set<ChannelId> unassignedDomain,
            Map<Long, Lobby> lobbyLookup,
            Map<ChannelId, Long> channelIDClientIDLookup,
            ChannelGroup managedClients
    ) {
        this.localPDUPipelines = localPDUPipelines;
        this.lobbyDomain = lobbyDomain;
        this.sessionDomain = sessionDomain;
        this.unassignedDomain = unassignedDomain;
        this.lobbyLookup = lobbyLookup;
        this.channelIDClientIDLookup = channelIDClientIDLookup;
        this.managedClients = managedClients;
    }

    public void init() {
        //ID - handshake with server
        //Outbound only PDU containing 64-bit Long clientID
        localPDUPipelines.put(PDUType.IDRES, new LocalPipeline().append(new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                IDRes idRes = (IDRes) in.getData();
                in.setData(Unpooled.buffer(Long.BYTES).writeLong(idRes.getNewClientID()));
                out.writeAndFlush(in);
            }
        }));
        /*--------LOBBY--------*/
        //Inbound PDU no payload and action
        localPDUPipelines.put(PDUType.CREATELOBBYREQ, new LocalPipeline().append(new PDUInboundHandler() {
            @Override
            public void handle(PDU p) {
                //Client is already connected to a lobby or is in a GameSession
                if (
                        lobbyDomain.keySet().stream().map(managedClients::find).map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                        ||
                        sessionDomain.keySet().stream().map(managedClients::find).map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                )
                    return;
                Channel lobbyUnassignClientChannel = unassignedDomain.stream().map(managedClients::find).filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
                Long lobbyID = lobbyIDAutoIncrement++; //todo: Redis?
                //Client is already assigned to a lobby
                if (lobbyUnassignClientChannel == null)
                    return;
                lobbyLookup.put(lobbyID, new Lobby((byte) 2, lobbyUnassignClientChannel.id()));
                unassignedDomain.remove(lobbyUnassignClientChannel.id());
                lobbyDomain.put(lobbyUnassignClientChannel.id(), lobbyID);
                System.out.printf("Client %s has created a lobby %d\n", channelIDClientIDLookup.get(lobbyUnassignClientChannel.id()), lobbyID);

                PDU outPDU = new PDU();
                CreateLobbyRes res = new CreateLobbyRes();
                outPDU.setPDUType(PDUType.CREATELOBBYRES);
                outPDU.setAddress(lobbyUnassignClientChannel.remoteAddress());
                res.setLobbyId(lobbyID);
                outPDU.setData(res);
                sendUnicast(outPDU);
                refreshLobbyList();
            }
        }));
        //Outbound only PDU 32-bit Integer payload
        localPDUPipelines.put(PDUType.CREATELOBBYRES, new LocalPipeline().append(new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                CreateLobbyRes createLobbyRes = (CreateLobbyRes) in.getData();
                in.setData(Unpooled.buffer(Long.BYTES).writeLong(createLobbyRes.getLobbyId()));
                out.writeAndFlush(in);
            }
        }));
        //Inbound only PDU with 8B Long data and action
        localPDUPipelines.put(PDUType.JOINLOBBYREQ, new LocalPipeline().append(new PDUHandlerDecoder() {
            @Override
            public void decode(PDU in, PDUInboundHandler out) {
                JoinLobbyReq joinLobbyReq = new JoinLobbyReq();
                joinLobbyReq.setLobbyID(((ByteBuf) in.getData()).readLong());
                in.setData(joinLobbyReq);
                out.handle(in);
            }}, new PDUInboundHandler(){

            @Override
            public void handle(PDU p) {
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
                sendUnicast(joinResPDU);
                JoinLobbyRes justJoinInfo = new JoinLobbyRes();
                justJoinInfo.setLobbyID(-1L);

                PDU jJIPDU = new PDU();
                jJIPDU.setPDUType(PDUType.CREATELOBBYRES);
                jJIPDU.setAddress(unassignLobbyCh.remoteAddress());
                jJIPDU.setData(justJoinInfo);
                sendMulticastLobby(jJIPDU);
                //todo: send to lobbyDomain Multicast to notify other lobby members
                refreshLobbyList();
            }
        }));
        //Outbound only PDU no action with 8B Long data
        localPDUPipelines.put(PDUType.JOINLOBBYRES, new PDUInboundHandler() {
            @Override
            public ByteBuf encode(Object in) {
                JoinLobbyRes joinLobbyRes = (JoinLobbyRes) in;
                return Unpooled.buffer(Long.BYTES).writeLong(joinLobbyRes.getLobbyID());
            }
        });
        //Inbound PDU with no payload and with action
        localPDUPipelines.put(PDUType.LEAVELOBBYREQ, new PDUHandlerEncoder() {
            @Override
            public void handle(PDU p) {
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
                        sendUnicast(pLeave);
                    }
                } else {
                    lobbyID = lobbyDomain.remove(assignedLobbyChannel.id());
                    unassignedDomain.add(assignedLobbyChannel.id());
                    lobby.leave(assignedLobbyChannel.id());

                    Channel lobbyLeadCh = findDomainChannel(lobby.getLeaderID()).orElse(null);
                    if (lobbyLeadCh == null) {
                        System.err.printf("Could not find lobby %d leader %d and send disconnect info", lobbyID, transformChID(lobby.getLeaderID()));
                    } else {
                        LeaveLobbyRes justLeaveInfo = new LeaveLobbyRes();
                        justLeaveInfo.setLeader(true);

                        PDU lobbyLeaveLeaderInfo = new PDU();
                        lobbyLeaveLeaderInfo.setPDUType(PDUType.LEAVELOBBYRES);
                        lobbyLeaveLeaderInfo.setAddress(lobbyLeadCh.remoteAddress());
                        lobbyLeaveLeaderInfo.setData(justLeaveInfo);
                        sendUnicast(lobbyLeaveLeaderInfo);
                    }
                }
                LeaveLobbyRes normalLeave = new LeaveLobbyRes();
                normalLeave.setLeader(false);
                PDU pLeave = new PDU();
                pLeave.setPDUType(PDUType.LEAVELOBBYRES);
                pLeave.setAddress(assignedLobbyChannel.remoteAddress());
                pLeave.setData(normalLeave);
                sendUnicast(pLeave);
                System.out.printf("Client %d has left lobby %d\n", channelIDClientIDLookup.get(assignedLobbyChannel.id()), lobbyID);
                refreshLobbyList();
            }
        });
        //Outbound PDU with 1B Boolean data or action
        localPDUPipelines.put(PDUType.LEAVELOBBYRES, new PDUInboundHandler() {
            @Override
            public ByteBuf encode(Object in) {
                LeaveLobbyRes leaveLobbyRes = (LeaveLobbyRes) in;
                return Unpooled.buffer(1).writeBoolean(leaveLobbyRes.isLeader());
            }
        });
        //Outbound only PDU with no action, 8B Long, 2 * 1B Byte data and 1B Byte - Boolean Lobby list refresh flag
        localPDUPipelines.put(PDUType.LOBBYBEACON, new PDUInboundHandler() {
            @Override
            public ByteBuf encode(Object in) {
                LobbyBeacon lobbyBeacon = (LobbyBeacon) in;
                return Unpooled.buffer(Long.BYTES + 3 * Byte.BYTES).writeLong(lobbyBeacon.getLobbyID()).writeByte(lobbyBeacon.getLobbyCurOccupancy()).writeByte(lobbyBeacon.getLobbyMaxOccupancy()).writeBoolean(lobbyBeacon.getLobbyListRefresh());
            }
        });
    }

    private void joinLobby(PDU p) {

    }

    private void leaveLobby(PDU p) {

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
            sendBroadcastUnassigned(outherBOPDU);
            sendBroadcastLobby(outherBOPDU);
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
            sendBroadcastUnassigned(outherBOPDU);
            sendBroadcastLobby(outherBOPDU);
        }
    }

    private void sendUnicast(PDU p) {
        unassignedDomain.stream().map(managedClients::find).filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> c.writeAndFlush(p));
        lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> c.writeAndFlush(p));
        sessionDomain.keySet().stream().map(managedClients::find).filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> c.writeAndFlush(p));
    }

    private void sendBroadcast(PDU p) {
        sendBroadcastUnassigned(p);
        sendBroadcastLobby(p);
        sessionDomain.keySet().stream().map(managedClients::find).filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> c.writeAndFlush(p));
    }

    private void sendBroadcastUnassigned(PDU p) {
        unassignedDomain.stream().map(managedClients::find).filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> c.writeAndFlush(p));
    }

    private void sendBroadcastLobby(PDU p) {
        lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> localPDUPipelines.get(p.getPDUType()).ingest(p, c));
    }

    private void sendMulticastLobby(PDU p) {
        lobbyDomain.entrySet().stream().filter(e -> managedClients.find(e.getKey()).remoteAddress().equals(p.getAddress())).map(Map.Entry::getValue).forEach(l ->
                lobbyDomain.entrySet().stream().filter(e -> !managedClients.find(e.getKey()).remoteAddress().equals(p.getAddress()) && e.getValue().equals(l)).map(Map.Entry::getKey).map(managedClients::find).forEach(c -> c.writeAndFlush(p))
        );
    }

    private Long transformChID(ChannelId chID) {
        return channelIDClientIDLookup.get(chID);
    }

    private Optional<Channel> findDomainChannel(ChannelId chID) {
        return unassignedDomain.stream().map(managedClients::find).filter(c -> c.id().equals(chID)).findAny()
                .or(() -> lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> c.id().equals(chID)).findAny())
                .or(() -> sessionDomain.keySet().stream().map(managedClients::find).filter(c -> c.id().equals(chID)).findAny());
    }
}
