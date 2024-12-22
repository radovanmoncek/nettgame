package server.game.docker.client.ship.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import server.game.docker.client.modules.lobby.facades.LobbyChannelFacade;
import server.game.docker.client.modules.messages.facades.ChatMessageChannelFacade;
import server.game.docker.client.modules.player.facades.PlayerChannelFacade;
import server.game.docker.client.modules.sessions.facades.SessionChannelFacade;
import server.game.docker.client.modules.state.facades.StateChannelFacade;
import server.game.docker.client.ship.parents.facades.ChannelFacade;
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
    private PlayerChannelFacade playerClientFacade;
    private LobbyChannelFacade lobbyClientFacade;
    private SessionChannelFacade sessionClientFacade;
    private StateChannelFacade stateClientFacade;
    private ChatMessageChannelFacade chatMessageClientFacade;

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
     * @param reconnectIntervalSeconds the number of seconds between reconnect attempts.
     * @param failReconnectAfterAttempts the number of reconnect retries.
     */
    public void run(int reconnectIntervalSeconds, int failReconnectAfterAttempts) {
        try {
            bootstrap = bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new GameClientChannelInitializer(
                            playerClientFacade,
                            lobbyClientFacade,
                            sessionClientFacade,
                            stateClientFacade,
                            chatMessageClientFacade
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
        for (int i = 0; i < failReconnectAfterAttempts && serverChannel == null; i++) {
            try {
                serverChannel = bootstrap.connect(gameServerAddress, gameServerPort).sync().channel();
                TimeUnit.SECONDS.sleep(reconnectIntervalSeconds);
            } catch (Exception ignored) {
            }
        }

        playerClientFacade = Objects.requireNonNullElse(playerClientFacade, new PlayerChannelFacade());
        lobbyClientFacade = Objects.requireNonNullElse(lobbyClientFacade, new LobbyChannelFacade());
        sessionClientFacade = Objects.requireNonNullElse(sessionClientFacade, new SessionChannelFacade());
        stateClientFacade = Objects.requireNonNullElse(stateClientFacade, new StateChannelFacade());
        chatMessageClientFacade = Objects.requireNonNullElse(chatMessageClientFacade, new ChatMessageChannelFacade());

        injectServerChannelIntoClientFacade(playerClientFacade);
        injectServerChannelIntoClientFacade(lobbyClientFacade);
        injectServerChannelIntoClientFacade(sessionClientFacade);
        injectServerChannelIntoClientFacade(stateClientFacade);
        injectServerChannelIntoClientFacade(chatMessageClientFacade);
    }

    public static GameClient newInstance() throws Exception {
        if (Objects.isNull(INSTANCE))
            INSTANCE = new GameClient();

        return INSTANCE;
    }

    public GameClient withPlayerClientFacade(final PlayerChannelFacade playerClientFacade) {
        if(this.playerClientFacade != null)
            return this;

        if (playerClientFacade == null)
            throw new NullPointerException("usernameClientFacade is null");

        this.playerClientFacade = playerClientFacade;
        return this;
    }

    public GameClient withLobbyClientFacade(final LobbyChannelFacade lobbyClientFacade) {
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

    public GameClient withSessionClientFacade(final SessionChannelFacade sessionClientFacade) {
        this.sessionClientFacade = sessionClientFacade;
        return this;
    }

    public GameClient withStateClientFacade(final StateChannelFacade stateClientFacade) {
        this.stateClientFacade = stateClientFacade;
        return this;
    }

    public GameClient withChatMessageClientFacade(ChatMessageChannelFacade chatMessageClientFacade) {
        this.chatMessageClientFacade = chatMessageClientFacade;
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

    public PlayerChannelFacade getUsernameClientFacade() {
        return playerClientFacade;
    }

    public LobbyChannelFacade getLobbyFacade() {
        return lobbyClientFacade;
    }

    public SessionChannelFacade getSessionClientFacade() {
        return sessionClientFacade;
    }

    public StateChannelFacade getStateClientFacade() {
        return stateClientFacade;
    }

    public ChatMessageChannelFacade getChatMessageFacade() {
        return chatMessageClientFacade;
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

    private void injectServerChannelIntoClientFacade(final ChannelFacade<? extends PDU> channelFacade) {
        var clazz = channelFacade.getClass().getSuperclass();
        while (Stream.of(clazz.getDeclaredFields()).map(Field::getType).noneMatch(Channel.class::equals)) {
            clazz = clazz.getSuperclass();
        }
        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(Channel.class))
                .findAny().ifPresent(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(channelFacade, serverChannel);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }
}
