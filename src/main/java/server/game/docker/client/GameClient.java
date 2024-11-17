package server.game.docker.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.client.modules.player.facades.PlayerClientFacade;
import server.game.docker.client.modules.sessions.facades.SessionClientFacade;
import server.game.docker.client.modules.state.facades.StateClientFacade;
import server.game.docker.client.ship.parents.facades.ClientFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * <p>
 *     Singleton class representing a client instance connecting to a given DockerGameServer.
 * </p>
 */
public final class GameClient {
    /**
     * Singleton instance
     */
    private static GameClient INSTANCE;
    private final EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private InetAddress gameServerAddress;
    private int gameServerPort;
    private Channel serverChannel;
    private PlayerClientFacade playerClientFacade;
    private LobbyClientFacade lobbyClientFacade;
    private SessionClientFacade sessionClientFacade;
    private StateClientFacade stateClientFacade;

    /**
     * Constructs a new {@link GameClient} instance with an ip address of 127.0.0.1 and port number of 4321.
     *
     * @throws UnknownHostException if the {@link InetAddress} fails to resolve
     */
    private GameClient() throws UnknownHostException {
        gameServerAddress = InetAddress.getByName("127.0.0.1");
        gameServerPort = 4321;
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
    }

    /**
     * @param reconnectIntervalSeconds
     */
    public void run(int reconnectIntervalSeconds) {
        try {
            bootstrap = bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ClientInitializer(
                            playerClientFacade,
                            lobbyClientFacade,
                            sessionClientFacade,
                            stateClientFacade
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
        while (serverChannel == null) {
            try {
                serverChannel = bootstrap.connect(gameServerAddress, gameServerPort).sync().channel();
                TimeUnit.SECONDS.sleep(reconnectIntervalSeconds);
            } catch (Exception ignored) {
            }
        }
        injectServerChannelIntoClientFacade(playerClientFacade);
        injectServerChannelIntoClientFacade(lobbyClientFacade);
        injectServerChannelIntoClientFacade(sessionClientFacade);
        injectServerChannelIntoClientFacade(stateClientFacade);
    }

    public static GameClient newInstance() throws Exception {
        if (Objects.isNull(INSTANCE))
            INSTANCE = new GameClient();

        return INSTANCE;
    }

    public GameClient withPlayerClientFacade(final PlayerClientFacade playerClientFacade) {
        if(this.playerClientFacade != null)
            return this;

        if (playerClientFacade == null)
            throw new NullPointerException("usernameClientFacade is null");

        this.playerClientFacade = playerClientFacade;
        return this;
    }

    public GameClient withLobbyClientFacade(final LobbyClientFacade lobbyClientFacade) {
        if (this.lobbyClientFacade != null)
            return this;

        if (lobbyClientFacade == null) {
            throw new NullPointerException("lobbyRequestClientFacade cannot be null");
        }

        this.lobbyClientFacade = lobbyClientFacade;
        return this;
    }

    public GameClient withServerAddress(final InetAddress gameServerAddress) {
        this.gameServerAddress = gameServerAddress;
        return this;
    }

    public GameClient withServerPort(final int gameServerPort) {
        this.gameServerPort = gameServerPort;
        return this;
    }

    public GameClient withSessionClientFacade(final SessionClientFacade sessionClientFacade) {
        this.sessionClientFacade = sessionClientFacade;
        return this;
    }

    public GameClient withStateClientFacade(final StateClientFacade stateClientFacade) {
        this.stateClientFacade = stateClientFacade;
        return this;
    }

    public InetAddress getServerAddress() {
        return gameServerAddress;
    }

    public void setServerChannel(final Channel serverChannel) {
        this.serverChannel = serverChannel;
        injectServerChannelIntoClientFacade(playerClientFacade);
        injectServerChannelIntoClientFacade(lobbyClientFacade);
    }

    public PlayerClientFacade getUsernameClientFacade() {
        return playerClientFacade;
    }

    public LobbyClientFacade getLobbyFacade() {
        return lobbyClientFacade;
    }

    public SessionClientFacade getSessionClientFacade() {
        return sessionClientFacade;
    }

    public StateClientFacade getStateClientFacade() {
        return stateClientFacade;
    }

    public int getGameServerPort() {
        return gameServerPort;
    }

    public void shutdownGracefullyAfterNSeconds(final int seconds) throws InterruptedException {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                serverChannel.close();
            }
        }, (long) seconds * 1000);
        serverChannel.closeFuture().sync();
        workerGroup.shutdownGracefully();
    }

    public boolean isConnected() {
        return serverChannel != null && serverChannel.isActive();
    }

    private void injectServerChannelIntoClientFacade(final ClientFacade<? extends PDU> clientFacade) {
        var clazz = clientFacade.getClass().getSuperclass();
        while (Stream.of(clazz.getDeclaredFields()).map(Field::getType).noneMatch(Channel.class::equals)) {
            clazz = clazz.getSuperclass();
        }
        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(Channel.class))
                .findAny().ifPresent(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(clientFacade, serverChannel);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }
}
