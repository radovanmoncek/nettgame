package server.game.docker.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.net.pipelines.PDUMultiPipeline;
import server.game.docker.net.modules.decoders.GameDecoder;
import server.game.docker.net.modules.encoders.GameEncoder;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.enums.PDUType;
import server.game.docker.server.net.handlers.LobbyPDUInboundHandler.Lobby;
import server.game.docker.server.session.DockerGameSession;
import server.game.docker.server.net.handlers.GameServerHandler;

import java.util.*;

/**
 *
 */
public class GameServer {
    private final int port;
    private final PDUMultiPipeline multiPipeline;
    private final ChannelGroup managedClients;
    private final Set<ChannelId> unassignedDomain;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<ChannelId, Long> sessionDomain; //todo: Docker in phase 4
    private final Map<Long, Lobby> lobbyLookup;
    /**
     * GameSession identified by Long Lobby id
     * Because Session ID = Match ID (for persistence only)
     */
    private final Map<Long, DockerGameSession> sessionLookup;
    /**
     * Arbitrary channel / client ID - transformer for debugging purposes (later will originate from database)
     */
    private final Map<ChannelId, Long> channelIDClientIDLookup;
    private Long autoIncrementClientID = 1L;
    private Long autoIncrementDebugClientID = 1L;

    public GameServer(int port) {
        this.port = port;
        managedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        unassignedDomain = new HashSet<>();
        lobbyDomain = new HashMap<>();
        sessionDomain = new HashMap<>();
        lobbyLookup = new HashMap<>();
        sessionLookup = new HashMap<>();
        channelIDClientIDLookup = new HashMap<>();
        multiPipeline = new PDUMultiPipeline();
    }

    public GameServer(String [] args) {
        this(4321);
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new LoggingHandler(LogLevel.ERROR),
                                    new GameDecoder(),
                                    new GameEncoder(),
                                    new GameServerHandler(
                                            channelIDClientIDLookup,
                                            multiPipeline,
                                            managedClients,
                                            lobbyDomain,
                                            lobbyLookup,
                                            sessionDomain,
                                            unassignedDomain,
                                            GameServer.this
                                    )
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            new GameServerInitializer(
                    multiPipeline,
                    lobbyDomain,
                    sessionDomain,
                    unassignedDomain,
                    lobbyLookup,
                    channelIDClientIDLookup,
                    managedClients,
                    this
            ).init();

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.printf("GameServer running on port %d\n", port);

            //Blocking method
            future.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void sendUnicast(PDUType type, PDU protocolDataUnit, Channel channel) {
        unassignedDomain.stream().map(managedClients::find).filter(c -> c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.ingest(type, protocolDataUnit, c));
        lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.ingest(type, protocolDataUnit, c));
        sessionDomain.keySet().stream().map(managedClients::find).filter(c -> c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.ingest(type ,protocolDataUnit, c));
    }

    private void sendBroadcast(PDUType type, PDU protocolDataUnit, Channel channel) {
        sendBroadcastUnassigned(protocolDataUnit);
        sendBroadcastLobby(protocolDataUnit);
        sessionDomain.keySet().stream().map(managedClients::find).filter(c -> !c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.ingest(type, protocolDataUnit, c));
    }

    public void sendBroadcastUnassigned(PDU p) {
        unassignedDomain.stream().map(managedClients::find).filter(c -> !c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.ingest(type, p, c));
    }

    public void sendBroadcastLobby(PDU p) {
        lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> !c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.ingest(type, p, c));
    }

    public void sendMulticastLobby(PDUType lobbyupdate, PDU p, Channel channel) {
        lobbyDomain.entrySet().stream().filter(e -> managedClients.find(e.getKey()).remoteAddress().equals(channel.remoteAddress())).map(Map.Entry::getValue).forEach(l ->
                lobbyDomain.entrySet().stream().filter(e -> !managedClients.find(e.getKey()).remoteAddress().equals(channel.remoteAddress()) && e.getValue().equals(l)).map(Map.Entry::getKey).map(managedClients::find).forEach(c -> multiPipeline.ingest(type, p, c))
        );
    }

    public Long transformChID(ChannelId chID) {
        return channelIDClientIDLookup.get(chID);
    }

    public Optional<Channel> findDomainChannel(ChannelId chID) {
        return unassignedDomain.stream().map(managedClients::find).filter(c -> c.id().equals(chID)).findAny()
                .or(() -> lobbyDomain.keySet().stream().map(managedClients::find).filter(c -> c.id().equals(chID)).findAny())
                .or(() -> sessionDomain.keySet().stream().map(managedClients::find).filter(c -> c.id().equals(chID)).findAny());
    }

    public synchronized Long getNextLobbyID(){
        return autoIncrementClientID++;
    }

    public synchronized Long getNextDebugClientID() {
        return autoIncrementDebugClientID++;
    }
}
