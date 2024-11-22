package server.game.docker;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.modules.chat.facades.ChatMessageServerFacade;
import server.game.docker.modules.lobby.facades.LobbyServerFacade;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.modules.session.facades.SessionServerFacade;
import server.game.docker.modules.state.facades.StateServerFacade;
import server.game.docker.ship.parents.facades.ServerFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p>
 * The root class of the DockerGameServer, it initializes and encapsulates the Netty protocol server and its functionality.
 * </p>
 * <p>
 * The default port number of the DockerGameServer is TCP 4321.
 * INSTANCE value may be changed, please refer to {@link #withPort(Integer port)} for further information.
 * </p>
 * <p>
 * Please make sure to note that because of the multi-threaded nature of {@link EventLoopGroup} workers,
 * any methods utilizing their functionality are declared as synchronized.
 * </p>
 */
public final class GameServer {
    /**
     * Singleton pattern
     */
    private static GameServer INSTANCE;
    /**
     * The server port. Defaults to 4321.
     */
    private Integer port = 4321;
    /**
     * All the clients connected to INSTANCE server instance
     */
    private final ChannelGroup managedClients;
    /**
     *
     */
    private PlayerServerFacade playerServerFacade;
    /**
     *
     */
    private LobbyServerFacade lobbyServerFacade;
    private ChannelFuture future;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private Supplier<SessionServerFacade> sessionServerFacadeFactory;
    private StateServerFacade stateServerFacade;
    private ChatMessageServerFacade chatMessageServerFacade;

    private GameServer() {
        managedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        playerServerFacade = new PlayerServerFacade();
        lobbyServerFacade = new LobbyServerFacade(playerServerFacade);
        sessionServerFacadeFactory = () -> {
            throw new RuntimeException("SessionServerFacade was not supplied. Expect incorrect operation.");
        };
    }

    public static GameServer newInstance() {
        if (INSTANCE == null)
            INSTANCE = new GameServer();

        return INSTANCE;
    }

    public GameServer withPort(final Integer port) {
        if (port <= 1024)
            throw new IllegalArgumentException("Port number must not belong to the interval [1, 1024], which is a reserved port pool, or be lesser than its lowest bound.");
        this.port = port;
        return this;
    }

    public GameServer withUsernameServerFacade(final PlayerServerFacade usernameServerFacade) {
        this.playerServerFacade = usernameServerFacade;
        return this;
    }

    public GameServer withLobbyRequestServerFacade(final LobbyServerFacade lobbyServerFacade) {
        this.lobbyServerFacade = lobbyServerFacade;
        return this;
    }

    public GameServer withSessionServerFacadeFactory(final Supplier<SessionServerFacade> sessionServerFacadeFactory) {
        this.sessionServerFacadeFactory = () -> {
            final var sessionServerFacade = sessionServerFacadeFactory.get();
            injectManagedClientsIntoServerFacade(sessionServerFacade);
            return sessionServerFacade;
        };
        return this;
    }

    public GameServer withStateServerFacade(final StateServerFacade stateServerFacade) {
        this.stateServerFacade = stateServerFacade;
        return this;
    }

    public GameServer withChatMessageServerFacade(final ChatMessageServerFacade chatMessageServerFacade) {
        this.chatMessageServerFacade = chatMessageServerFacade;
        return this;
    }

    /**
     * <p>
     * Runs the DockerGameServer instance and blocks the current thread until the {@link #shutdownGracefullyAfterNSeconds shutdownGracefullyAfterNSeconds(int seconds)} method is called.
     * </p>
     *
     * @throws InterruptedException if the {@link ChannelFuture#sync()} is interrupted
     */
    public void run() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        final var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new GameServerInitializer(
                        playerServerFacade,
                        lobbyServerFacade,
                        sessionServerFacadeFactory,
                        stateServerFacade,
                        chatMessageServerFacade
                ))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            future = bootstrap.bind(port).sync();
            System.out.printf("GameServer running on port %d\n", port);

            injectManagedClientsIntoServerFacade(playerServerFacade);
            injectManagedClientsIntoServerFacade(lobbyServerFacade);
            injectManagedClientsIntoServerFacade(stateServerFacade);
            injectManagedClientsIntoServerFacade(chatMessageServerFacade);

            future.channel().closeFuture().sync();
        } finally {
            shutdownGracefullyAfterNSeconds(0);
        }
    }

    public void shutdownGracefullyAfterNSeconds(final int seconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                future.channel().close();
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }, (long) seconds * 1000);
    }

    private void injectManagedClientsIntoServerFacade(ServerFacade<? extends PDU> serverFacade) {
        Class<?> clazz = serverFacade.getClass();
        while (Stream.of(clazz.getDeclaredFields()).map(Field::getType).noneMatch(ChannelGroup.class::equals)) {
            clazz = clazz.getSuperclass();
        }

        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(ChannelGroup.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(serverFacade, managedClients);
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }
}
