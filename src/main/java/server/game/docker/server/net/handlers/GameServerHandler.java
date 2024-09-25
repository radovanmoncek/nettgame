package server.game.docker.server.net.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.net.*;
import server.game.docker.net.dto.*;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;
import server.game.docker.server.matchmaking.Lobby;
import server.game.docker.server.matchmaking.session.GameSession;

import java.net.InetSocketAddress;
import java.util.*;

public class GameServerHandler extends ChannelInboundHandlerAdapter {
    private final PDUHandler pDUHandler;
    private static final ChannelGroup unassignedDomain = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Map<Channel, Long> lobbyDomain = new HashMap<>();
    private static final Map<Channel, Long> sessionDomain = new HashMap<>(); //todo: Docker in phase 4

    private static final Map<Long, Lobby> lobbyLookup = new HashMap<>();
    /**
     * GameSession identified by Long Lobby id
     * Because Session ID = Match ID (for persistence only)
     */
    private static final Map<Long, GameSession> sessionLookup = new HashMap<>();

    /**
     * Arbitrary channel / client ID - transformer
     */
    private static final Map<ChannelId, Long> channelIDClientIDLookup = new HashMap<>();

    private static Long lobbyIDAutoIncrement = 1L;

    public GameServerHandler(PDUHandler pDUHandler) {
        this.pDUHandler = pDUHandler
                //ID - handshake with server
                //Outbound only PDU containing 64-bit Long clientID
                .appendPipeline(PDUType.IDRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        IDRes idRes = (IDRes) in;
                        return Unpooled.buffer(/*8*/Long.BYTES).writeLong(idRes.getNewClientID()/*.byteValue()}*/);
                    }
                })
                //LOBBY
                //Inbound PDU no payload and action
                .appendPipeline(PDUType.CREATELOBBYREQ, new AbstractLocalActionPipeline() {
                    @Override
                    public void handle(PDU p) {
                        //Client is already connected to a lobby or is in a GameSession
                        if (
                                lobbyDomain.keySet().stream().map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                                ||
                                sessionDomain.keySet().stream().map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                        )
                            return;
                        Channel lobbyUnassignClientChannel = unassignedDomain.stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
                        Long lobbyID = lobbyIDAutoIncrement++; //todo: Redis?
                        //Client is already assigned to a lobby
                        if(lobbyUnassignClientChannel == null)
                            return;
                        lobbyLookup.put(lobbyID, new Lobby((byte) 2, lobbyUnassignClientChannel.id()));
                        unassignedDomain.remove(lobbyUnassignClientChannel);
                        lobbyDomain.put(lobbyUnassignClientChannel, lobbyID);
                        System.out.printf("Client %s has created a lobby %d\n", channelIDClientIDLookup.get(lobbyUnassignClientChannel.id()), lobbyID);

                        sendUnicast(new PDU(PDUType.CREATELOBBYRES, p.getAddress(), p.getPort(), lobbyID));
                        refreshLobbyList();
                    }
                })
                //Outbound only PDU 32-bit Integer payload
                .appendPipeline(PDUType.CREATELOBBYRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        Long l = (Long) in;
                        return Unpooled.buffer(Long.BYTES).writeLong(l);
                    }
                })
                //Inbound only PDU with 8B Long data and action
                .appendPipeline(PDUType.JOINLOBBYREQ, new AbstractLocalInboundActionPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        JoinLobbyReq out = new JoinLobbyReq();
                        out.setLobbyID(in.readLong());
                        return out;
                    }

                    @Override
                    public void handle(PDU p) {
                        JoinLobbyReq joinLobbyReq = (JoinLobbyReq) p.getData();
                        if(lobbyLookup.get(joinLobbyReq.getLobbyID()) == null || lobbyLookup.get(joinLobbyReq.getLobbyID()).isFull())
                            return;
                        Channel unassignLobbyCh = unassignedDomain.stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
                        if(unassignLobbyCh == null) {
                            unassignLobbyCh = lobbyDomain.keySet().stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
                            if (unassignLobbyCh == null || lobbyLookup.get(joinLobbyReq.getLobbyID()).getClientIDs().contains(unassignLobbyCh.id()))
                                return;
                            lobbyLookup.get(lobbyDomain.remove(unassignLobbyCh)).leave(unassignLobbyCh.id());
                        }
                        else
                            unassignedDomain.remove(unassignLobbyCh);
                        lobbyDomain.put(unassignLobbyCh, joinLobbyReq.getLobbyID());
                        lobbyLookup.get(joinLobbyReq.getLobbyID()).join(unassignLobbyCh.id());
                        JoinLobbyRes joinLobbyRes = new JoinLobbyRes();
                        joinLobbyRes.setLobbyID(joinLobbyReq.getLobbyID());
                        sendUnicast(new PDU(PDUType.JOINLOBBYRES, unassignLobbyCh.remoteAddress(), ((InetSocketAddress) unassignLobbyCh.remoteAddress()).getPort(), joinLobbyRes));
                        JoinLobbyRes justJoinInfo = new JoinLobbyRes();
                        justJoinInfo.setLobbyID(-1L);
                        sendMulticastLobby(new PDU(PDUType.JOINLOBBYRES, unassignLobbyCh.remoteAddress(), ((InetSocketAddress) unassignLobbyCh.remoteAddress()).getPort(), justJoinInfo));
                        //todo: send to lobbyDomain Multicast to notify other lobby members
                        refreshLobbyList();
                    }
                })
                //Outbound only PDU no action with 8B Long data
                .appendPipeline(PDUType.JOINLOBBYRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        JoinLobbyRes joinLobbyRes = (JoinLobbyRes) in;
                        return Unpooled.buffer(Long.BYTES).writeLong(joinLobbyRes.getLobbyID());
                    }
                })
                //Inbound PDU with no payload and with action
                .appendPipeline(PDUType.LEAVELOBBYREQ, new AbstractLocalActionPipeline() {
                    @Override
                    public void handle(PDU p) {
                        Channel assignedLobbyChannel = lobbyDomain.keySet().stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
                        if(assignedLobbyChannel == null)
                            return;
                        Lobby lobby = lobbyLookup.get(lobbyDomain.get(assignedLobbyChannel));
                        Long lobbyID;
                        if(lobby.getLeaderID().compareTo(assignedLobbyChannel.id()) == 0) {
                            ChannelId secondLobbyMember = lobbyLookup.remove(lobbyID = lobbyDomain.remove(assignedLobbyChannel)).getClientIDs().stream().filter(c -> !c.equals(lobby.getLeaderID())).findAny().orElse(null);
                            Channel sLMC = lobbyDomain.keySet().stream().filter(c -> c.id().equals(secondLobbyMember)).findAny().orElse(null);
                            unassignedDomain.add(assignedLobbyChannel);
                            if(sLMC != null) {
                                lobbyDomain.remove(sLMC);
                                unassignedDomain.add(sLMC);
                                lobby.leave(secondLobbyMember);
                                sendUnicast(new PDU(PDUType.LEAVELOBBYRES, sLMC.remoteAddress(), ((InetSocketAddress) sLMC.remoteAddress()).getPort(), null));
                            }
                        }
                        else {
                            lobbyID = lobbyDomain.remove(assignedLobbyChannel);
                            unassignedDomain.add(assignedLobbyChannel);
                            lobby.leave(assignedLobbyChannel.id());

                            Channel lobbyLeadCh = findDomainChannel(lobby.getLeaderID()).orElse(null);
                            if(lobbyLeadCh == null) {
                                System.err.printf("Could not find lobby %d leader %d and send disconnect info", lobbyID, transformChID(lobby.getLeaderID()));
                            }
                            else {
                                LeaveLobbyRes justLeaveInfo = new LeaveLobbyRes();
                                justLeaveInfo.setLeader(true);

                                PDU lobbyLeaveLeaderInfo = new PDU();
                                lobbyLeaveLeaderInfo.setPDUType(PDUType.LEAVELOBBYRES);
                                lobbyLeaveLeaderInfo.setAddress(lobbyLeadCh.remoteAddress());
                                lobbyLeaveLeaderInfo.setPort(((InetSocketAddress) lobbyLeadCh.remoteAddress()).getPort());
                                lobbyLeaveLeaderInfo.setData(justLeaveInfo);
                                sendUnicast(lobbyLeaveLeaderInfo);
                            }
                        }
                        LeaveLobbyRes normalLeave = new LeaveLobbyRes();
                        normalLeave.setLeader(false);
                        sendUnicast(new PDU(PDUType.LEAVELOBBYRES, assignedLobbyChannel.remoteAddress(), ((InetSocketAddress) assignedLobbyChannel.remoteAddress()).getPort(), /*null*/normalLeave));
                        System.out.printf("Client %d has left lobby %d\n", channelIDClientIDLookup.get(assignedLobbyChannel.id()), lobbyID);
                        refreshLobbyList();
                    }
                })
                //Outbound PDU with 1B Boolean data or action
                .appendPipeline(PDUType.LEAVELOBBYRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        LeaveLobbyRes leaveLobbyRes = (LeaveLobbyRes) in;
                        return Unpooled.buffer(1).writeBoolean(leaveLobbyRes.isLeader());
                    }
                })
                //Outbound only PDU with no action, 8B Long, 2 * 1B Byte data and 1B Byte - Boolean Lobby list refresh flag
                .appendPipeline(PDUType.LOBBYBEACON, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        LobbyBeacon lobbyBeacon = (LobbyBeacon) in;
                        return Unpooled.buffer(Long.BYTES + 3 * Byte.BYTES).writeLong(lobbyBeacon.getLobbyID()).writeByte(lobbyBeacon.getLobbyCurOccupancy()).writeByte(lobbyBeacon.getLobbyMaxOccupancy()).writeBoolean(lobbyBeacon.getLobbyListRefresh());
                    }
                });
    }

    private Long transformChID(ChannelId chID) {
        return channelIDClientIDLookup.get(chID);
    }

    private Optional<Channel> findDomainChannel(ChannelId chID) {
        return unassignedDomain.stream().filter(c -> c.id().equals(chID)).findAny()
                .or(() -> lobbyDomain.keySet().stream().filter(c -> c.id().equals(chID)).findAny())
                .or(() -> sessionDomain.keySet().stream().filter(c -> c.id().equals(chID)).findAny());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        PDU pdu = (PDU) msg;
        pDUHandler.receive(pdu);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        unassignedDomain.add(ctx.channel());
        System.out.println("A client has requested an ID");
        //Body data payload is empty for this PDUType
        IDRes idRes = new IDRes();
        long newClientID = 0L;
        for (byte b: ctx.channel().id().asShortText().getBytes())
            newClientID += b;
        idRes.setNewClientID(newClientID);
        PDU pdu = new PDU(PDUType.IDRES, ctx.channel().remoteAddress(), ((InetSocketAddress) ctx.channel().remoteAddress()).getPort(), idRes);
        sendUnicast(pdu);
        System.out.printf("A client has connected with assigned ID: %d\n", idRes.getNewClientID());
        channelIDClientIDLookup.put(ctx.channel().id(), newClientID);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        unassignedDomain.remove(ctx.channel());
        lobbyLookup.remove(lobbyDomain.remove(ctx.channel())); //todo: handle
        sessionDomain.remove(ctx.channel()); //todo: handle
        System.out.printf("Client %d has disconnected\n", channelIDClientIDLookup.remove(ctx.channel().id()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {}

    private void sendUnicast(PDU p){
        unassignedDomain.stream().filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> pDUHandler.send(c, p));
        lobbyDomain.keySet().stream().filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> pDUHandler.send(c, p));
        sessionDomain.keySet().stream().filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> pDUHandler.send(c, p));
    }

    private void sendBroadcast(PDU p){
        sendBroadcastUnassigned(p);
        sendBroadcastLobby(p);
        sessionDomain.keySet().stream().filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> pDUHandler.send(c, p));
    }

    private void sendBroadcastUnassigned(PDU pDU){
        unassignedDomain.stream().filter(c -> !c.remoteAddress().equals(pDU.getAddress())).forEach(c -> pDUHandler.send(c, pDU));
    }

    private void  sendBroadcastLobby(PDU p){
        lobbyDomain.keySet().stream().filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> pDUHandler.send(c, p));
    }

    private void sendMulticastLobby(PDU p){
        lobbyDomain.entrySet().stream().filter(e -> e.getKey().remoteAddress().equals(p.getAddress())).map(Map.Entry::getValue).forEach(l ->
            lobbyDomain.entrySet().stream().filter(e -> !e.getKey().remoteAddress().equals(p.getAddress()) && e.getValue().equals(l)).map(Map.Entry::getKey).forEach(c -> pDUHandler.send(c, p))
        );
    }

    private void joinLobby(PDU p){

    }

    private void leaveLobby(PDU p){

    }

    private void refreshLobbyList(){
        if(lobbyLookup.isEmpty()){
            LobbyBeacon beacon = new LobbyBeacon();
            beacon.setLobbyListRefresh(true);
            beacon.setLobbyID(-1L);
            beacon.setLobbyCurOccupancy((byte) -1);
            beacon.setLobbyMaxOccupancy((byte) -1);
            PDU outherBOPDU = new PDU(PDUType.LOBBYBEACON, null, null, beacon);
            sendBroadcastUnassigned(outherBOPDU);
            sendBroadcastLobby(outherBOPDU);
            return;
        }
        boolean refFlag = true;
        for(Map.Entry<Long, Lobby> e : lobbyLookup.entrySet()) {
            LobbyBeacon beacon = new LobbyBeacon();
            beacon.setLobbyListRefresh(refFlag);
            if(refFlag)
                refFlag = false;
            beacon.setLobbyID(e.getKey());
            beacon.setLobbyCurOccupancy(e.getValue().getCurrentSize());
            beacon.setLobbyMaxOccupancy(e.getValue().getMaxSize());
            PDU outherBOPDU = new PDU(PDUType.LOBBYBEACON, null, null, beacon);
            sendBroadcastUnassigned(outherBOPDU);
            sendBroadcastLobby(outherBOPDU);
        }
    }
}
