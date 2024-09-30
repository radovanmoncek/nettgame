package server.game.docker.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.client.net.handlers.GameClientHandler;
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.decoders.GameDecoder;
import server.game.docker.net.dto.*;
import server.game.docker.net.encoders.GameEncoder;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class GameClient {
    private Bootstrap bootstrap;
    private final InetAddress gameServerAddress;
    private final int gameServerPort;
    private final EventLoopGroup workerGroup;
    private final Map<PDUType, LocalPipeline> localPDUPipelines;
    private final Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings;
    private Channel clientChannel;
//    todo: private Long clientID; ?

    public GameClient(String [] args) throws Exception {
        gameServerAddress = InetAddress.getByName("127.0.0.1");
        gameServerPort = 4321;
        workerGroup = new NioEventLoopGroup();
        localPDUPipelines = new HashMap<>();
        eventMappings = new HashMap<>();
        new ClientInitializer(clientChannel, localPDUPipelines, eventMappings).init();
        try {
            bootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new LoggingHandler(LogLevel.ERROR),
                                    new GameEncoder(),
                                    new GameDecoder(),
                                    new GameClientHandler(localPDUPipelines));
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

    private void sendUnicast(PDU p) {
        p.setData(localPDUPipelines.get(p.getPDUType()).encode(p.getData()));
        clientChannel.writeAndFlush(p);
    }

    /*--------API methods--------*/
    public void connect() throws Exception {
        clientChannel = bootstrap.connect(gameServerAddress, gameServerPort).sync().channel();

        System.out.println("Connected to the server");
    }

    public boolean isConnected(){
        return clientChannel != null && clientChannel.isActive();
    }

    public void disconnect() throws Exception {
        clientChannel.close();
        clientChannel.closeFuture().sync();
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

    public void setOnIDReceived(ClientAPIEventHandler<IDRes> eventHandler){
        eventMappings.put(ClientAPIEventType.CONNECTED, eventHandler);
    }

    public void setOnLobbyCreate(ClientAPIEventHandler<CreateLobbyRes> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYCREATED, eventHandler);
    }

    public void setOnLobbyJoined(ClientAPIEventHandler<JoinLobbyRes> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYJOINED, eventHandler);
    }

    public void setOnLobbyLeave(ClientAPIEventHandler<LeaveLobbyRes> eventHandler){
//        eventHandler.accept();

        eventMappings.put(ClientAPIEventType.MEMBERLEFT, eventHandler);
//        ((ClientAPIEventHandler<JoinLobbyRes>) eventMappings.get(ClientAPIEventType.MEMBERLEFT)).handle(new JoinLobbyRes());
    }

    public void setOnLobbyBeacon(ClientAPIEventHandler<LobbyBeacon> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYBEACON, eventHandler);
    }

    /**
     * Attempts to request creation of a personal lobby.
     */
    public void createLobby(){
        PDU p = new PDU();
        p.setPDUType(PDUType.CREATELOBBYREQ);
        sendUnicast(p);
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
        p.setAddress(new InetSocketAddress(gameServerAddress, gameServerPort));
        p.setData(localPDUPipelines.get(p.getPDUType()).encode(r));
        clientChannel.writeAndFlush(p);
    }

    /**
     * Attempts to leave current lobby.
     */
    public void leaveLobby(){
        PDU p = new PDU();
        p.setPDUType(PDUType.LEAVELOBBYREQ);
        sendUnicast(p);
    }

    public void sendLobbyChatMessage(){

    }
}
