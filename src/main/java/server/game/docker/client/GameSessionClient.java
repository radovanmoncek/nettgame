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
import server.game.docker.net.pipelines.PDUMultiPipeline;
import server.game.docker.net.modules.decoders.ProtocolDecoder;
import server.game.docker.net.modules.encoders.ProtocolEncoder;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.enums.PDUType;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class GameSessionClient {
    private Bootstrap bootstrap;
    private final InetAddress gameServerAddress;
    private final int gameServerPort;
    private final EventLoopGroup workerGroup;
    private final PDUMultiPipeline multiPipeline;
    private final Map<ClientAPIEventType, ClientAPIEventHandler<? extends PDU>> eventMappings;
    private Channel clientChannel;

    //    todo: private Long clientID; ?
    public GameSessionClient(String [] args) throws Exception {
        gameServerAddress = InetAddress.getByName("127.0.0.1");
        gameServerPort = 4321;
        workerGroup = new NioEventLoopGroup();
        multiPipeline = new PDUMultiPipeline();
        eventMappings = new HashMap<>();
        new ClientInitializer(clientChannel, eventMappings, this, multiPipeline).init();
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
                                    new ProtocolEncoder(),
                                    new ProtocolDecoder(),
                                    new GameClientHandler(multiPipeline));
                        }
                    });
        }
        catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
    }
    public void sendUnicast(PDUType type, PDU protocolDataUnit) {
        multiPipeline.ingest(type, protocolDataUnit, clientChannel);
    }

    public <T extends PDU> void checkAndCallHandler(ClientAPIEventType eventType, T protocolDataUnit) {
        ClientAPIEventHandler<?> h = eventMappings.get(eventType);
//        Type t = h.getClass().getGenericInterfaces()[0];
//        if(((ParameterizedType) h.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].equals(((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0]))
        ((ClientAPIEventHandler<T>) h).handle(protocolDataUnit);
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

    public PDUMultiPipeline getMultiPipeline() {
        return multiPipeline;
    }

    public Map<ClientAPIEventType, ClientAPIEventHandler<?>> getEventMappings() {
        return eventMappings;
    }

    public void setClientChannel(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }
}
