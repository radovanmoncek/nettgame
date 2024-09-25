package server.game.docker.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.net.decoders.GameDecoder;
import server.game.docker.net.dto.JoinLobbyReq;
import server.game.docker.net.dto.JoinLobbyRes;
import server.game.docker.net.dto.LeaveLobbyRes;
import server.game.docker.net.encoders.GameEncoder;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.PDUHandler;
import server.game.docker.client.net.handlers.GameClientHandler;
import server.game.docker.net.pdu.PDUType;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class GameClient {
    private Bootstrap bootstrap;
    private final InetAddress gameServerAddress;
    private final int gameServerPort;
    private final EventLoopGroup workerGroup;
    @Deprecated
    private final PDUHandler pDUHandler;
    private final Map<PDUType, LocalPipeline> localPipelines;
    private final Map<ClientAPIEventType, Object> eventMappings;
    private Channel channel;
//    private Long clientID;

    public GameClient(String [] args) throws Exception {
        pDUHandler = new PDUHandler();
        gameServerAddress = InetAddress.getByName("127.0.0.1");
        gameServerPort = 4321;
        workerGroup = new NioEventLoopGroup();
        localPipelines = new HashMap<>();
        eventMappings = new HashMap<>();
        new ClientAPIInitializer(localPipelines, eventMappings);
        try {
            bootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.ERROR), new GameEncoder(), new GameDecoder(), new GameClientHandler(pDUHandler, localPipelines, eventMappings));
                        }
                    });

                    //Outbound only PDU
//                    .withMapping(PDUType.IDREQUEST, new LocalPipeline() {
//                        @Override
//                        public Object decode(ByteBuf in) {
//                            return null;
//                        }
//
//                        @Override
//                        public ByteBuf encode(Object in) {
//                            return Unpooled.wrappedBuffer(new byte[]{0, PDUType.IDREQUEST.getID()});
//                        }
//
//                        @Override
//                        public void perform(PDUBody body) {
//                        }
//                    });

            //Initiate "ID handshake"
//            sendUnicast(new PDUBody(/*PDUType.JOIN, new Join()*/PDUType.IDREQUEST, new InetSocketAddress(gameServerAddress, gameServerPort), gameServerPort, null)).sync();
        }
        catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
    }

    public /*ChannelFuture*/void sendUnicast(PDU pDU) {
//        return channel.writeAndFlush(pDU);
        pDUHandler.send(channel, pDU);
//        ByteBuf out = mappings.get(p.getPDUType()).encode(p.getData());
//        p.setByteBuf(out);
//        c.writeAndFlush(p);
    }

    public void connect() throws Exception {
        channel = bootstrap.connect(gameServerAddress, gameServerPort).sync().channel();

        System.out.println("Connected to the server");
    }

    public boolean isConnected(){
        return channel != null && channel.isActive();
    }

    public void disconnect() throws Exception {
        channel.close();
        channel.closeFuture().sync();
        workerGroup.shutdownGracefully();
    }

    //todo: temp class, client will be launched from JavaFX gui
//    public static void main(String[] args) throws Exception {
//        new GameClient(args);
//    }

    /**
     * <p>
     *     The ClientID value assigned to this GameClient by the server.
     * </p>
     * @return Long clientID
     */
//    public Long getClientID() {
//        return clientID;
//    }

    /**
     * <p>
     *     This method enables the registration of a {@link PDUType} to this {@link GameClient} along with its encoder, decoder, and IoC (Inversion of Control) action,
     *     which then together form a pipeline process determined by the specific {@link LocalPipeline} implementation.
     * </p>
     * <p>
     *     Since {@link PDUHandler} uses a {@link HashMap} internally, it is not possible to duplicate any existing entry by this method.
     *     Therefore any added {@link PDUType} handling is unique.
     * </p>
     * @param t
     * @param p
     * @return {@link PDUHandler} for convenient chaining
     */
    @Deprecated
    public PDUHandler registerPDU(PDUType t, LocalPipeline p) {
        return pDUHandler.appendPipeline(t, p);
    }

    public void setOnLobbyLeave(/*Consumer*/ClientAPIEventHandler<LeaveLobbyRes> eventHandler){
//        eventHandler.accept();

        eventMappings.put(ClientAPIEventType.MEMBERLEFT, eventHandler);
//        ((ClientAPIEventHandler<JoinLobbyRes>) eventMappings.get(ClientAPIEventType.MEMBERLEFT)).handle(new JoinLobbyRes());
    }

    /**
     * Attempts to join a specified lobby.
     * @param lobbyID the ID of the desired lobby
     */
    public void joinLobby(Long lobbyID){
        PDU p = new PDU();
        JoinLobbyReq r = new JoinLobbyReq();
        r.setLobbyID(lobbyID);
        p.setPDUType(PDUType.JOINLOBBYREQ);
//        p.setAddress(gameServerAddress);
        p.setPort(gameServerPort);
        p.setData(r);
        sendUnicast(p);
    }

    /**
     * Attempts to leave current lobby.
     */
    public void leaveLobby(){

    }

    public void sendLobbyChatMessage(){

    }
}
