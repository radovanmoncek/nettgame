package server.game.docker.ship.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.modules.chat.facades.ChatMessageChannelGroupFacade;
import server.game.docker.modules.lobby.facades.LobbyChannelGroupFacade;
import server.game.docker.modules.player.facades.PlayerChannelGroupFacade;
import server.game.docker.modules.session.facades.SessionChannelGroupFacade;
import server.game.docker.modules.state.facades.StateChannelGroupFacade;
import server.game.docker.ship.parents.facades.ChannelGroupFacade;
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
 * any methods utilizing their functionality are to be declared as synchronized.
 * </p>
 */
public final class GameServerContainer {
    /**
     * Singleton pattern
     */
    private static GameServerContainer INSTANCE;
    /**
     * The server port. Defaults to 4321.
     */
    private Integer port = 4321;
    /**
     * All the client channels connected to this server
     */
    private final ChannelGroup managedClients;
    /**
     *
     */
    private PlayerChannelGroupFacade playerServerFacade;
    /**
     *
     */
    private LobbyChannelGroupFacade lobbyServerFacade;
    private ChannelFuture future;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private StateChannelGroupFacade stateServerFacade;
    private ChatMessageChannelGroupFacade chatMessageChannelGroupFacade;
    private Supplier<SessionChannelGroupFacade> sessionServerFacadeFactory;
    private StateChannelGroupFacade stateChannelGroupFacade;
    private boolean master;
    private final ThreadGroup managedSessions;

    private GameServerContainer() {
        master = true;
        managedSessions = new ThreadGroup("Managed Sessions");
        sessionServerFacadeFactory = () -> {
            throw new RuntimeException("SessionServerFacade was not supplied. Expect incorrect operation.");
        };
        stateChannelGroupFacade = new StateChannelGroupFacade();
        managedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        playerServerFacade = new PlayerChannelGroupFacade();
        lobbyServerFacade = new LobbyChannelGroupFacade(playerServerFacade);
        chatMessageChannelGroupFacade = new ChatMessageChannelGroupFacade();
        stateServerFacade = new StateChannelGroupFacade();
    }

    public static GameServerContainer newInstance() {
        if (INSTANCE == null)
            INSTANCE = new GameServerContainer();

        return INSTANCE;
    }

    public GameServerContainer withPort(final Integer port) {
        if (port <= 1024)
            throw new IllegalArgumentException("Port number must not belong to the interval [1, 1024], which is a reserved port pool, or be lesser than its lowest bound.");
        this.port = port;
        return this;
    }

    public GameServerContainer withSessionServerFacadeFactory(final Supplier<SessionChannelGroupFacade> sessionServerFacadeFactory) {
        this.sessionServerFacadeFactory = () -> {
            final var sessionServerFacade = sessionServerFacadeFactory.get();
            injectManagedClientsIntoChannelGroupFacade(sessionServerFacade);
            return sessionServerFacade;
        };
        return this;
    }

    public GameServerContainer withStateServerFacade(final StateChannelGroupFacade stateChannelGroupFacade) {
        this.stateChannelGroupFacade = stateChannelGroupFacade;
        return this;
    }

    public GameServerContainer withUsernameServerFacade(final PlayerChannelGroupFacade usernameServerFacade) {
        this.playerServerFacade = usernameServerFacade;
        return this;
    }

    public GameServerContainer withLobbyRequestServerFacade(final LobbyChannelGroupFacade lobbyServerFacade) {
        this.lobbyServerFacade = lobbyServerFacade;
        return this;
    }

    public GameServerContainer withChatMessageServerFacade(final ChatMessageChannelGroupFacade chatMessageChannelGroupFacade) {
        this.chatMessageChannelGroupFacade = chatMessageChannelGroupFacade;
        return this;
    }

    /**
     * <p>
     * Runs the DockerGameServer instance and blocks the current thread until the {@link #shutdownGracefullyAfterNSeconds shutdownGracefullyAfterNSeconds(int seconds)} method is called.
     * </p>
     *
     * @throws InterruptedException if the {@link ChannelFuture#sync()} is interrupted
     */
    public void run(int shutdownSeconds) throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        final var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new GameServerContainerChannelInitializer(
                        playerServerFacade,
                        lobbyServerFacade,
                        chatMessageChannelGroupFacade,
                        sessionServerFacadeFactory,
                        stateChannelGroupFacade,
                        managedSessions
                ))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            future = bootstrap.bind(port).sync();
            System.out.printf("GameServer running on port %d\n", port);

            injectManagedClientsIntoChannelGroupFacade(playerServerFacade);
            injectManagedClientsIntoChannelGroupFacade(lobbyServerFacade);
            injectManagedClientsIntoChannelGroupFacade(chatMessageChannelGroupFacade);
            injectManagedClientsIntoChannelGroupFacade(stateChannelGroupFacade);

            future.channel().closeFuture().sync();
        } finally {
            shutdownGracefullyAfterNSeconds(shutdownSeconds);
        }
    }

    public void run() throws InterruptedException {
        run(0);
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

    private void injectManagedClientsIntoChannelGroupFacade(ChannelGroupFacade<? extends PDU> channelGroupFacade) {
        Class<?> clazz = channelGroupFacade.getClass();
        while (Stream.of(clazz.getDeclaredFields()).map(Field::getType).noneMatch(ChannelGroup.class::equals)) {
            clazz = clazz.getSuperclass();
        }

        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(ChannelGroup.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(channelGroupFacade, managedClients);
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }
}
