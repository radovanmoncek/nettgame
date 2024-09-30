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
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.decoders.GameDecoder;
import server.game.docker.net.encoders.GameEncoder;
import server.game.docker.net.pdu.PDUType;
import server.game.docker.server.matchmaking.Lobby;
import server.game.docker.server.matchmaking.session.GameSession;
import server.game.docker.server.net.handlers.GameServerHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class GameServer {
    private final int port;
    private final Map<PDUType, LocalPipeline> localPDUPipelines;
    private final ChannelGroup managedCleints;
    private final Set<ChannelId> unassignedDomain;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<ChannelId, Long> sessionDomain; //todo: Docker in phase 4
    private final Map<Long, Lobby> lobbyLookup;
    /**
     * GameSession identified by Long Lobby id
     * Because Session ID = Match ID (for persistence only)
     */
    private final Map<Long, GameSession> sessionLookup;
    /**
     * Arbitrary channel / client ID - transformer for debugging purposes (later will originate from database)
     */
    private final Map<ChannelId, Long> channelIDClientIDLookup;
    private final Long autoIncrementClientID = 1L;

    public GameServer(int port) {
        this.port = port;
        managedCleints = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        unassignedDomain = new HashSet<>();
        lobbyDomain = new HashMap<>();
        sessionDomain = new HashMap<>();
        lobbyLookup = new HashMap<>();
        sessionLookup = new HashMap<>();
        channelIDClientIDLookup = new HashMap<>();
        localPDUPipelines = new HashMap<>();
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
                                            localPDUPipelines,
                                            managedCleints,
                                            lobbyDomain,
                                            lobbyLookup,
                                            sessionDomain,
                                            unassignedDomain,
                                            autoIncrementClientID)
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            new GameServerInitializer(
                    localPDUPipelines,
                    lobbyDomain,
                    sessionDomain,
                    unassignedDomain,
                    lobbyLookup,
                    channelIDClientIDLookup,
                    managedCleints
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
}
