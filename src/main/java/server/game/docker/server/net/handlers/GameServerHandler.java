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
import server.game.docker.net.dto.IDRes;
import server.game.docker.net.dto.JoinLobbyReq;
import server.game.docker.net.dto.JoinLobbyRes;
import server.game.docker.net.dto.LobbyBeacon;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;
import server.game.docker.server.matchmaking.Lobby;
import server.game.docker.server.matchmaking.session.GameSession;

import java.net.InetSocketAddress;
import java.util.*;

public class GameServerHandler extends ChannelInboundHandlerAdapter {
    private final PDUHandler pDUHandler;
//    @Deprecated
//    private final List<Long> connectedClients;
//    @Deprecated
//    private final List<Lobby> lobbies;
//    @Deprecated
//    private final Set<Long> servedClients;
    private static final ChannelGroup unassignedDomain = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Map<Channel, Long> lobbyDomain = new HashMap<>();
    private static final Map<Channel, Long> sessionDomain = new HashMap<>(); //todo: Docker in phase 4

    private static final Map<Long, Lobby> lobbyLookup = new HashMap<>();
    /**
     * GameSession identified by Long Lobby id
     *
     * Because Session ID = Match ID (for persistence only)
     */
    private static final Map<Long, GameSession> sessionLookup = new HashMap<>();

    /**
     * Arbitrary channel / client ID - transformer
     */
    private static final Map<ChannelId, Long> channelIDClientIDLookup = new HashMap<>();

    private static Long lobbyIDAutoIncrement = 1L;

    public GameServerHandler(PDUHandler pDUHandler) {
//        lobbyAssignedChannels = new HashMap<>();
//        sessionAssignedChannels = new HashMap<>();
//        connectedClients = new LinkedList<>();
//        lobbies = new LinkedList<>();
//        servedClients = new HashSet<>();
//        lobbyUnassignedChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
//        lobbyLookup = new HashMap<>();
//        channelIDClientIDLookup = new HashMap<>();
        this.pDUHandler = pDUHandler
                //Inbound PDU only, incoming buffer payload value is irrelevant
//                .withMapping(PDUType.IDREQUEST, new LocalPipeline() {
//                    @Override
//                    public Object decode(ByteBuf in) {return null;}
//
//                    @Override
//                    public ByteBuf encode(Object in) {return null;}
//
//                    @Override
//                    public void perform(PDUBody body) {
//                        System.out.println("ID request received");
//                    }
//                })
//                .registerPDU(PDUType.JOIN, new LocalPipeline() {
//                   @Override
//                    public Object decode(ByteBuf in) {
//                        Join out = new Join();
//                        in = in.slice(2, in.readableBytes());
//                        out.setClientID(in.readLong());
//                        return out;
//                    }
//
//                    @Override
//                    public ByteBuf encode(Object in) {
//                        Join join = (Join) in;
//                        ByteBuf out;
//                        if(Objects.isNull(join.getClientID()))
//                            out = Unpooled.wrappedBuffer(new byte[]{0, PDUType.JOIN.getID()});
//                        else
//                            out = Unpooled.wrappedBuffer(new byte[]{0, PDUType.JOIN.getID(), join.getClientID().byteValue()});
//                        return out;
//                    }
//
//                    @Override
//                    public void perform(PDU p) {
//                        Join joinDTO = (Join) p.getData();
//                        if(joinDTO.getClientID() == null){
//                            IDRes idRes = new IDRes();
//                            long newClientID;
//                            connectedClients.add(newClientID = connectedClients.size() + 1L);
//                            idResponse.setNewClientID(newClientID);
//                                future.channel().writeAndFlush(new Vector<>(List.of(PDUType.IDREQUEST, idResponse))).addListener(ChannelFutureListener.CLOSE);
//                        }
//                            if(!servedClients.add(joinDTO.getClientID()))
//                                return;
//                            lobbies.add(new Lobby((byte) 2, joinDTO.getClientID(), handler));
//                    }
//                })
//                .registerPDU(PDUType.CHATMESSAGE, new LocalPipeline() {
//                    @Override
//                    public Object decode(ByteBuf in) {
//                        return null;
//                    }

//                    @Override
//                    public ByteBuf encode(Object in) {
//                        return null;
//                    }

//                    @Override
//                    public void perform(PDU p) {
//                        ChatMessage chatMessage = (ChatMessage) p.getData();

//                    }
//                })
                //ID - handshake with server
                //Outbound only PDU containing 64-bit Long clientID
                .registerPDU(PDUType.IDRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        IDRes idRes = (IDRes) in;
                        return Unpooled.buffer(/*8*/Long.BYTES).writeLong(/*wrappedBuffer(new byte[] {0, PDUType.IDRES.getID(), */idRes.getNewClientID()/*.byteValue()}*/);
                    }
                })
                //LOBBY
                //Inbound PDU no payload and action
                .registerPDU(PDUType.CREATELOBBYREQ, new AbstractLocalActionPipeline() {
                    @Override
                    public void perform(PDU p) {
//                        SocketAddress address = p.getAddress();
                        //Client is already connected to a lobby or is in a GameSession
                        if (
                                lobbyDomain.keySet().stream().map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                                &&
                                sessionDomain.keySet().stream().map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                        )
                            return;
                        Channel lobbyUnassignClientChannel = unassignedDomain.stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null)/*.find(channelIDClientIDLookup.get())*/;
//                        Long clientID = channelIDClientIDLookup.get(address);
                        /*Integer*/Long lobbyID = /*lobbyAssignedChannels.size() + 1*/lobbyIDAutoIncrement++; //todo: Redis?
                        //Client is already assigned to a lobby
//                        if(lobbyLookup.containsKey(/*clientID*/lobbyID))
//                            return;
                        if(lobbyUnassignClientChannel == null)
                            return;
                        lobbyLookup.put(lobbyID, new Lobby((byte) 2, lobbyUnassignClientChannel.id()));
                        unassignedDomain.remove(lobbyUnassignClientChannel);
                        lobbyDomain.put(lobbyUnassignClientChannel, lobbyID);
                        System.out.printf("Client %s has joined a lobby %d\n", channelIDClientIDLookup.get(lobbyUnassignClientChannel.id()), lobbyID);

//                        lobbyAssignedChannels.put(lobbyID, new Lobby((byte) 2, clientID));
//                        lobbyLookup.put(clientID, lobbyID);
                        sendUnicast(new PDU(PDUType.CREATELOBBYRES, p.getAddress(), p.getPort(), lobbyID));
//                        sendBroadcastLobbyUnassigned(new PDU());
                        LobbyBeacon lobbyBeacon = new LobbyBeacon();
                        lobbyBeacon.setLobbyID(lobbyID);
                        lobbyBeacon.setLobbyCurOccupancy((byte) 1);
                        lobbyBeacon.setLobbyMaxOccupancy((byte) 2);
                        lobbyBeacon.setLobbyListRefresh(true);
                        PDU beaconOutPDU = new PDU(PDUType.LOBBYBEACON, null, null, lobbyBeacon);
                        sendBroadcastUnassigned(beaconOutPDU);
                        sendBroadcastLobby(beaconOutPDU);
                        lobbyLookup.forEach((i, l) -> {
                            if(i.equals(lobbyID))
                                return;
                            LobbyBeacon beacon = new LobbyBeacon();
                            beacon.setLobbyListRefresh(false);
                            beacon.setLobbyID(i);
                            beacon.setLobbyCurOccupancy(l.getCurrentSize());
                            beacon.setLobbyMaxOccupancy(l.getMaxSize());
                            PDU outherBOPDU = new PDU(PDUType.LOBBYBEACON, null, null, beacon);
                            sendBroadcastUnassigned(outherBOPDU);
                            sendBroadcastLobby(outherBOPDU);
                        });
                    }
                })
                //Outbound only PDU 32-bit Integer payload
                .registerPDU(PDUType.CREATELOBBYRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        Long l = (Long) in;
//                        ByteBuf out = Unpooled.wrappedBuffer(ByteBuffer.allocate(6).put((byte) 0).put(PDUType.CREATELOBBYRES.getID()).putInt(integer).array());
//                        byte [] temp = out.array();
                        return /*out*/Unpooled.buffer(Long.BYTES)/*.writeByte(PDUType.CREATELOBBYRES.getID())*/.writeLong(l);
                    }
                })
                //Inbound only PDU with 8B Long data and action
                .registerPDU(PDUType.JOINLOBBYREQ, new AbstractLocalInboundActionPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        JoinLobbyReq out = new JoinLobbyReq();
                        out.setLobbyID(in.readLong());
                        return out;
                    }

                    @Override
                    public void perform(PDU p) {
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
                        boolean sendRefresh = true;
                        if(lobbyLookup.isEmpty()){
                            LobbyBeacon beacon = new LobbyBeacon();
                            beacon.setLobbyListRefresh(sendRefresh);
                            beacon.setLobbyID(-1L);
                            beacon.setLobbyCurOccupancy((byte) -1);
                            beacon.setLobbyMaxOccupancy((byte) -1);
                            PDU outherBOPDU = new PDU(PDUType.LOBBYBEACON, null, null, beacon);
                            sendBroadcastUnassigned(outherBOPDU);
                            sendBroadcastLobby(outherBOPDU);
                        }
                        for(Map.Entry<Long, Lobby> e : lobbyLookup.entrySet()) {
//                            if(e.getKey().equals(lobbyID)) //redundant
//                                return;
                            LobbyBeacon beacon = new LobbyBeacon();
                            beacon.setLobbyListRefresh(sendRefresh);
                            if(sendRefresh)
                                sendRefresh = false;
                            beacon.setLobbyID(e.getKey());
                            beacon.setLobbyCurOccupancy(e.getValue().getCurrentSize());
                            beacon.setLobbyMaxOccupancy(e.getValue().getMaxSize());
                            PDU outherBOPDU = new PDU(PDUType.LOBBYBEACON, null, null, beacon);
                            sendBroadcastUnassigned(outherBOPDU);
                            sendBroadcastLobby(outherBOPDU);
                        }
                    }
                })
                //Outbound only PDU no action with 8B Long data
                .registerPDU(PDUType.JOINLOBBYRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        JoinLobbyRes joinLobbyRes = (JoinLobbyRes) in;
                        return Unpooled.buffer(Long.BYTES).writeLong(joinLobbyRes.getLobbyID());
                    }
                })
                //Inbound PDU with no payload and with action
                .registerPDU(PDUType.LEAVELOBBYREQ, new AbstractLocalActionPipeline() {
                    @Override
                    public void perform(PDU p) {
                        Channel assignedLobbyChannel = lobbyDomain.keySet().stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
                        if(assignedLobbyChannel == null)
                            return;
                        Lobby lobby = lobbyLookup.get(lobbyDomain.get(assignedLobbyChannel));
                        Long lobbyID;
                        if(lobby.getLeaderID().compareTo(assignedLobbyChannel.id()) == 0) {
                            ChannelId secondLobbyMember = lobbyLookup.remove(lobbyID = lobbyDomain.remove(assignedLobbyChannel)).getClientIDs().stream().filter(c -> !c.equals(lobby.getLeaderID())).findAny().orElse(null);
                            Channel sLMC = lobbyDomain.keySet().stream().filter(c -> c.id().equals(secondLobbyMember)).findAny().orElse(null);
                            if(sLMC == null)
                                return;
                            lobbyDomain.remove(sLMC);
                            unassignedDomain.add(sLMC);
                            lobby.leave(secondLobbyMember);
                            sendUnicast(new PDU(PDUType.LEAVELOBBYRES, sLMC.remoteAddress(), ((InetSocketAddress) sLMC.remoteAddress()).getPort(), null));
                        }
                        else
                            lobbyID = lobbyDomain.remove(assignedLobbyChannel);
                        unassignedDomain.add(assignedLobbyChannel);
                        lobby.leave(assignedLobbyChannel.id());
                        sendUnicast(new PDU(PDUType.LEAVELOBBYRES, assignedLobbyChannel.remoteAddress(), ((InetSocketAddress) assignedLobbyChannel.remoteAddress()).getPort(), null));
                        System.out.printf("Client %d has left lobby %d\n", channelIDClientIDLookup.get(assignedLobbyChannel.id()), lobbyID);
                        boolean sendRefresh = true;
                        if(lobbyLookup.isEmpty()){
                            LobbyBeacon beacon = new LobbyBeacon();
                            beacon.setLobbyListRefresh(sendRefresh);
                            beacon.setLobbyID(-1L);
                            beacon.setLobbyCurOccupancy((byte) -1);
                            beacon.setLobbyMaxOccupancy((byte) -1);
                            PDU outherBOPDU = new PDU(PDUType.LOBBYBEACON, null, null, beacon);
                            sendBroadcastUnassigned(outherBOPDU);
                            sendBroadcastLobby(outherBOPDU);
                        }
                        for(Map.Entry<Long, Lobby> e : lobbyLookup.entrySet()) {
//                            if(e.getKey().equals(lobbyID)) //redundant
//                                return;
                            LobbyBeacon beacon = new LobbyBeacon();
                            beacon.setLobbyListRefresh(sendRefresh);
                            if(sendRefresh)
                                sendRefresh = false;
                            beacon.setLobbyID(e.getKey());
                            beacon.setLobbyCurOccupancy(e.getValue().getCurrentSize());
                            beacon.setLobbyMaxOccupancy(e.getValue().getMaxSize());
                            PDU outherBOPDU = new PDU(PDUType.LOBBYBEACON, null, null, beacon);
                            sendBroadcastUnassigned(outherBOPDU);
                            sendBroadcastLobby(outherBOPDU);
                        }
                    }
                })
                //Outbound PDU with no data or action
                .registerPDU(PDUType.LEAVELOBBYRES, new DefaultLocalPipeline())
                //Outbound only PDU with no action, 8B Long, 2 * 1B Byte data and 1B Byte - Boolean Lobby list refresh flag
                .registerPDU(PDUType.LOBBYBEACON, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        LobbyBeacon lobbyBeacon = (LobbyBeacon) in;
                        return Unpooled.buffer(Long.BYTES + 3 * Byte.BYTES).writeLong(lobbyBeacon.getLobbyID()).writeByte(lobbyBeacon.getLobbyCurOccupancy()).writeByte(lobbyBeacon.getLobbyMaxOccupancy()).writeBoolean(lobbyBeacon.getLobbyListRefresh());
                    }
                });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
//        super.channelRead(ctx, msg);
//        ByteBuf in = (ByteBuf) msg;
//        try {
//            System.out.println(msg);
//            while (in.isReadable()) {
//                System.out.print((char) in.readByte());
//                System.out.flush();
//            }
//        }
//        finally {
//            ReferenceCountUtil.release(msg);
//        }
//        ctx.write(msg);
//        ctx.flush();
        PDU pdu = (PDU) msg;
//        PDUHandler.map(pdu.getGameDataPDUType()).perform(pdu);
        pDUHandler.receive(pdu);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        unassignedDomain.add(ctx.channel());
        System.out.println("A client has requested an ID");
        //Body data payload is empty for this PDUType
        IDRes idRes = new IDRes();
        long newClientID = 0L;//Long.parseLong(/*Stream.of(IntStream.of(0, ctx.channel().id().asShortText().getBytes().length).map(i -> ctx.channel().id().asShortText().getBytes()[i]).boxed().map(b -> (int) b).reduce(Integer::sum)*/);//(long) lobbyUnassignedChannelGroup.stream().filter(c -> body.getAddress().equals(c.remoteAddress())).findAny().orElse(null).id().asShortText().length();//(long) lobbyUnassignedChannelGroup.size();
        for (byte b: ctx.channel().id().asShortText().getBytes())
            newClientID += b;
//        newClientID = ctx.channel().id().asShortText().getBytes()[0];
        idRes.setNewClientID(newClientID);
//                        connectedClients.add(newClientID);
        PDU pdu = new PDU(PDUType.IDRES, /*body.getAddress(), body.getPort(), idResponse*/ctx.channel().remoteAddress(), ((InetSocketAddress) ctx.channel().remoteAddress()).getPort(), idRes);
        sendUnicast(pdu);
        System.out.printf("A client has connected with assigned ID: %d\n", /*lobbyUnassignedChannelGroup.size()*/idRes.getNewClientID());
        channelIDClientIDLookup.put(ctx.channel().id()/*.remoteAddress()*/, newClientID);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        unassignedDomain.remove(ctx.channel());
        lobbyLookup.remove(lobbyDomain.remove(ctx.channel())); //todo: handle
        sessionDomain.remove(ctx.channel()); //todo: handle
        System.out.printf("Client %d has disconnected\n", channelIDClientIDLookup.remove(ctx.channel().id()));
//        channelIDClientIDLookup.remove(ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
//        final ByteBuf time = ctx.alloc().buffer(4);
//        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
//
//        final ChannelFuture f = ctx.writeAndFlush(time);
//        f.addListener(future -> {
//            assert f == future;
//            ctx.close();
//        });
//        ctx.writeAndFlush(/*new GameDataDecoder.TimeExampleDTO()*/new PDUBody(PDUType.IDRESPONSE, /*body.getAddress(), body.getPort(), idResponse*/null, null, null)).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendUnicast(PDU p){
        unassignedDomain.stream().filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> /*c.writeAndFlush(p)*/pDUHandler.send(c, p));
        lobbyDomain.keySet().stream().filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> /*c.writeAndFlush(p)*/pDUHandler.send(c, p));
        sessionDomain.keySet().stream().filter(c -> c.remoteAddress().equals(p.getAddress())).forEach(c -> /*c.writeAndFlush(p)*/pDUHandler.send(c, p));
    }

    private void sendBroadcast(PDU p){
//        unassignedDomain.stream().filter(c -> !c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
        sendBroadcastUnassigned(p);
//        lobbyDomain.keySet().stream().filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> /*c.writeAndFlush(p)*/pDUHandler.send(c, p));
        sendBroadcastLobby(p);
        sessionDomain.keySet().stream().filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> /*c.writeAndFlush(p)*/pDUHandler.send(c, p));
    }

    private void sendBroadcastUnassigned(PDU pDU){
        unassignedDomain.stream().filter(c -> !c.remoteAddress().equals(pDU.getAddress())).forEach(c -> /*c.writeAndFlush(pDU)*/pDUHandler.send(c, pDU));
    }

    private void  sendBroadcastLobby(PDU p){
        lobbyDomain.keySet().stream().filter(c -> !c.remoteAddress().equals(p.getAddress())).forEach(c -> /*c.writeAndFlush(p)*/pDUHandler.send(c, p));
    }

    private void sendMulticastLobby(PDU p){
        lobbyDomain.entrySet().stream().filter(e -> e.getKey().remoteAddress().equals(p.getAddress())).map(Map.Entry::getValue).forEach(l ->
            lobbyDomain.entrySet().stream().filter(e -> !e.getKey().remoteAddress().equals(p.getAddress()) && e.getValue().equals(l)).map(Map.Entry::getKey).forEach(c -> pDUHandler.send(c, p))
        );
    }
}
