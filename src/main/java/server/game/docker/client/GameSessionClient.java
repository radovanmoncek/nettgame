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
import server.game.docker.net.LocalPDUPipeline;
import server.game.docker.net.decoders.GameDecoder;
import server.game.docker.net.dto.*;
import server.game.docker.net.encoders.GameEncoder;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class GameSessionClient {
    private Bootstrap bootstrap;
    private final InetAddress gameServerAddress;
    private final int gameServerPort;
    private final EventLoopGroup workerGroup;
    private final Map<PDUType, LocalPDUPipeline> localPDUPipelines;
    private final Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings;
    private Channel clientChannel;

    //    todo: private Long clientID; ?
    public GameSessionClient(String [] args) throws Exception {
        gameServerAddress = InetAddress.getByName("127.0.0.1");
        gameServerPort = 4321;
        workerGroup = new NioEventLoopGroup();
        localPDUPipelines = new HashMap<>();
        eventMappings = new HashMap<>();
        new ClientInitializer(clientChannel, localPDUPipelines, eventMappings, this).init();
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
    public void sendUnicast(PDU p) {
        p.setAddress(new InetSocketAddress(gameServerAddress, gameServerPort));
        localPDUPipelines.get(p.getPDUType()).ingest(p, clientChannel);
    }

    public <T> ClientAPIEventHandler<T> checkAndGetHandler(Class<T> c, ClientAPIEventType eventType) {
        ClientAPIEventHandler<?> h = eventMappings.get(eventType);
//        Type t = h.getClass().getGenericInterfaces()[0];
//        if(((ParameterizedType) h.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].equals(((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0]))
            return (ClientAPIEventHandler<T>) h;
//        return null;
    }

    public final Channel getClientChannel() {
        return clientChannel;
    }

    public final Bootstrap getBootstrap() {
        return bootstrap;
    }

    public final InetAddress getServerAddress() {
        return gameServerAddress;
    }

    public int getGameServerPort() {
        return gameServerPort;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public Map<PDUType, LocalPDUPipeline> getLocalPDUPipelines() {
        return localPDUPipelines;
    }

    public Map<ClientAPIEventType, ClientAPIEventHandler<?>> getEventMappings() {
        return eventMappings;
    }

    public void setClientChannel(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }
}
