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
import server.game.docker.net.dto.ChatMessage;
import server.game.docker.net.dto.IDRes;
import server.game.docker.net.dto.Join;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;
import server.game.docker.server.matchmaking.Lobby;
import server.game.docker.server.matchmaking.session.GameSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class GameServerHandler extends ChannelInboundHandlerAdapter {
    private final PDUHandler PDUHandler;
//    @Deprecated
//    private final List<Long> connectedClients;
//    @Deprecated
//    private final List<Lobby> lobbies;
//    @Deprecated
//    private final Set<Long> servedClients;
    private static final ChannelGroup lobbyUnassignedChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Map<Channel, Long> lobbyAssignedChannels = new HashMap<>();
    private static final Map<Channel, GameSession> sessionAssignedChannels = new HashMap<>(); //todo: Docker in phase 4

    private static final Map<Long, Lobby> lobbyLookup = new HashMap<>();

    /**
     * Arbitrary channel / client ID - transformer
     */
    private static final Map<ChannelId, Long> channelIDClientIDLookup = new HashMap<>();

    private static Long lobbyIDAutoIncrement = 1L;

    public GameServerHandler(PDUHandler PDUHandler) {
//        lobbyAssignedChannels = new HashMap<>();
//        sessionAssignedChannels = new HashMap<>();
//        connectedClients = new LinkedList<>();
//        lobbies = new LinkedList<>();
//        servedClients = new HashSet<>();
//        lobbyUnassignedChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
//        lobbyLookup = new HashMap<>();
//        channelIDClientIDLookup = new HashMap<>();
        this.PDUHandler = PDUHandler
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
                .withMapping(PDUType.JOIN, new LocalPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        Join out = new Join();
                        in = in.slice(2, in.readableBytes());
                        out.setClientID(in.readLong());
                        return out;
                    }

                    @Override
                    public ByteBuf encode(Object in) {
                        Join join = (Join) in;
                        ByteBuf out;
                        if(Objects.isNull(join.getClientID()))
                            out = Unpooled.wrappedBuffer(new byte[]{0, PDUType.JOIN.getID()});
                        else
                            out = Unpooled.wrappedBuffer(new byte[]{0, PDUType.JOIN.getID(), join.getClientID().byteValue()});
                        return out;
                    }

                    @Override
                    public void perform(PDU p) {
                        Join joinDTO = (Join) p.getData();
                        if(joinDTO.getClientID() == null){
                            IDRes idRes = new IDRes();
                            long newClientID;
//                            connectedClients.add(newClientID = connectedClients.size() + 1L);
//                            idResponse.setNewClientID(newClientID);
//                                future.channel().writeAndFlush(new Vector<>(List.of(PDUType.IDREQUEST, idResponse))).addListener(ChannelFutureListener.CLOSE);
                        }
//                            if(!servedClients.add(joinDTO.getClientID()))
//                                return;
//                            lobbies.add(new Lobby((byte) 2, joinDTO.getClientID(), handler));
                    }
                })
                .withMapping(PDUType.CHATMESSAGE, new LocalPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        return null;
                    }

                    @Override
                    public ByteBuf encode(Object in) {
                        return null;
                    }

                    @Override
                    public void perform(PDU p) {
                        ChatMessage chatMessage = (ChatMessage) p.getData();

                    }
                })
                //Outbound only PDU containing 64-bit Long clientID
                .withMapping(PDUType.IDRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        IDRes idRes = (IDRes) in;
                        return Unpooled.buffer(/*8*/Long.BYTES).writeLong(/*wrappedBuffer(new byte[] {0, PDUType.IDRES.getID(), */idRes.getNewClientID()/*.byteValue()}*/);
                    }
                })
                //Inbound PDU no payload and action
                .withMapping(PDUType.CREATELOBBYREQ, new AbstractLocalActionPipeline() {
                    @Override
                    public void perform(PDU p) {
//                        SocketAddress address = p.getAddress();
                        //Client is already connected to a lobby or is in a GameSession
                        if (
                                lobbyAssignedChannels.keySet().stream().map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                                &&
                                sessionAssignedChannels.keySet().stream().map(Channel::remoteAddress).anyMatch(p.getAddress()::equals)
                        )
                            return;
                        Channel lobbyUnassignClientChannel = lobbyUnassignedChannelGroup.stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null)/*.find(channelIDClientIDLookup.get())*/;
//                        Long clientID = channelIDClientIDLookup.get(address);
                        /*Integer*/Long lobbyID = /*lobbyAssignedChannels.size() + 1*/lobbyIDAutoIncrement++; //todo: Redis?
                        //Client is already assigned to a lobby
//                        if(lobbyLookup.containsKey(/*clientID*/lobbyID))
//                            return;
                        if(lobbyUnassignClientChannel == null)
                            return;
                        lobbyLookup.put(lobbyID, new Lobby((byte) 2, lobbyUnassignClientChannel.id()));
                        lobbyUnassignedChannelGroup.remove(lobbyUnassignClientChannel);
                        lobbyAssignedChannels.put(lobbyUnassignClientChannel, lobbyID);
                        System.out.printf("Client %s has joined a lobby %d\n", channelIDClientIDLookup.get(lobbyUnassignClientChannel.id()), lobbyID);

//                        lobbyAssignedChannels.put(lobbyID, new Lobby((byte) 2, clientID));
//                        lobbyLookup.put(clientID, lobbyID);
                        sendUnicast(new PDU(PDUType.CREATELOBBYRES, p.getAddress(), p.getPort(), lobbyID));
//                        sendBroadcastLobbyUnassigned(new PDU());
                    }
                })
                //Outbound only PDU 32-bit Integer payload
                .withMapping(PDUType.CREATELOBBYRES, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        Long l = (Long) in;
//                        ByteBuf out = Unpooled.wrappedBuffer(ByteBuffer.allocate(6).put((byte) 0).put(PDUType.CREATELOBBYRES.getID()).putInt(integer).array());
//                        byte [] temp = out.array();
                        return /*out*/Unpooled.buffer(Long.BYTES)/*.writeByte(PDUType.CREATELOBBYRES.getID())*/.writeLong(l);
                    }
                })
                //Inbound PDU with no payload and with action
                .withMapping(PDUType.LEAVELOBBYREQ, new AbstractLocalActionPipeline() {
                    @Override
                    public void perform(PDU p) {
                        Channel assignedLobbyChannel = lobbyAssignedChannels.keySet().stream().filter(c -> p.getAddress().equals(c.remoteAddress())).findAny().orElse(null);
                        if(assignedLobbyChannel == null)
                            return;
                        Lobby lobby = lobbyLookup.get(lobbyAssignedChannels.get(assignedLobbyChannel));
                        Long lobbyID;
                        if(lobby.getLeaderID().compareTo(assignedLobbyChannel.id()) == 0)
                            lobbyLookup.remove(lobbyID = lobbyAssignedChannels.remove(assignedLobbyChannel));
                        else
                            lobbyID = lobbyAssignedChannels.remove(assignedLobbyChannel);
                        lobbyUnassignedChannelGroup.add(assignedLobbyChannel);
                        sendUnicast(new PDU(PDUType.LEAVELOBBYRES, assignedLobbyChannel.remoteAddress(), ((InetSocketAddress) assignedLobbyChannel.remoteAddress()).getPort(), null));
                        System.out.printf("Client %d has left lobby %d\n", channelIDClientIDLookup.get(assignedLobbyChannel.id()), lobbyID);
                    }
                })
                //Outbound PDU with no data or action
                .withMapping(PDUType.LEAVELOBBYRES, new DefaultLocalPipeline());
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
        PDUHandler.map(pdu.getGameDataPDUType()).perform(pdu);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        lobbyUnassignedChannelGroup.add(ctx.channel());
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
        lobbyUnassignedChannelGroup.remove(ctx.channel());
        lobbyLookup.remove(lobbyAssignedChannels.remove(ctx.channel())); //todo: handle
        sessionAssignedChannels.remove(ctx.channel()); //todo: handle
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

    public void sendUnicast(PDU pDU){
        lobbyUnassignedChannelGroup.stream().filter(c -> c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
        lobbyAssignedChannels.keySet().stream().filter(c -> c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
        sessionAssignedChannels.keySet().stream().filter(c -> c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
    }

    public void sendBroadcast(PDU pDU){
        lobbyUnassignedChannelGroup.stream().filter(c -> !c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
        lobbyAssignedChannels.keySet().stream().filter(c -> !c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
        sessionAssignedChannels.keySet().stream().filter(c -> !c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
    }

    public void sendBroadcastLobbyUnassigned(PDU pDU){
        lobbyUnassignedChannelGroup.stream().filter(c -> !c.remoteAddress().equals(pDU.getAddress())).forEach(c -> c.writeAndFlush(pDU));
    }
}
