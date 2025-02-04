package client.ship.bootstrap;

import client.modules.lobby.handlers.LobbyClientHandler;
import client.modules.messages.handlers.ChatMessageClientHandler;
import client.modules.player.handlers.PlayerClientHandler;
import client.modules.sessions.handlers.SessionClientHandler;
import client.modules.state.handlers.StateClientHandler;
import client.ship.parents.handlers.ChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import server.game.docker.ship.parents.pdus.PDU;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p>
 *     The Docker Game Client object enabling networked socket communication with the Docker Game Server.
 * </p>
 * <p>
 *     This class uses the <a href=https://refactoring.guru>Singleton and Builder Design Patterns</a>, since it is bound to an io resource.
 *     There can exist at most one instance of this class per runtime.
 * </p>
 */
public final class GameClient {
    /**
     * Singleton instance
     */
    private static GameClient INSTANCE;
    private final EventLoopGroup workerGroup;
    private InetAddress gameServerAddress;
    private int gameServerPort;
    private Channel serverChannel;
    private Supplier<ChannelHandler<? extends PDU>>
            playerClientHandlerSupplier,
            lobbyClientHandlerSupplier,
            sessionClientHandlerSupplier,
            stateClientHandlerSupplier,
            chatMessageClientHandlerSupplier;
    private Bootstrap bootstrap;

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
                            playerClientHandlerSupplier,
                            lobbyClientHandlerSupplier,
                            sessionClientHandlerSupplier,
                            stateClientHandlerSupplier,
                            chatMessageClientHandlerSupplier
                    ));
        } catch (Exception e) {
            e.printStackTrace(); //todo: log4j
            workerGroup.shutdownGracefully();
        }

        for (int i = 0; i < failReconnectAfterAttempts && serverChannel == null; i++) {
            try {
                serverChannel = bootstrap.connect(gameServerAddress, gameServerPort).sync().channel();
                TimeUnit.SECONDS.sleep(reconnectIntervalSeconds);
            } catch (Exception ignored) {
            }
        }

        playerClientHandlerSupplier = injectServerChannelIntoClientHandler(Objects.requireNonNullElse(playerClientHandlerSupplier, PlayerClientHandler::new));
        lobbyClientHandlerSupplier = injectServerChannelIntoClientHandler(Objects.requireNonNullElse(lobbyClientHandlerSupplier, LobbyClientHandler::new));
        sessionClientHandlerSupplier = injectServerChannelIntoClientHandler(Objects.requireNonNullElse(sessionClientHandlerSupplier, SessionClientHandler::new));
        stateClientHandlerSupplier = injectServerChannelIntoClientHandler(Objects.requireNonNullElse(stateClientHandlerSupplier, StateClientHandler::new));
        chatMessageClientHandlerSupplier = injectServerChannelIntoClientHandler(Objects.requireNonNullElse(chatMessageClientHandlerSupplier, ChatMessageClientHandler::new));
    }

    public static GameClient newInstance() throws Exception {
        if (Objects.isNull(INSTANCE))
            INSTANCE = new GameClient();

        return INSTANCE;
    }

    public GameClient withPlayerClientHandlerSupplier(final Supplier<ChannelHandler<? extends PDU>> playerClientFacade) {
        if(this.playerClientHandlerSupplier != null)
            return this;

        if (playerClientFacade == null)
            throw new NullPointerException("usernameClientFacade is null");

        this.playerClientHandlerSupplier = playerClientFacade;
        return this;
    }

    public GameClient withLobbyClientHandlerSupplier(final Supplier<ChannelHandler<? extends PDU>> lobbyClientFacade) {
        if (this.lobbyClientHandlerSupplier != null)
            return this;

        if (lobbyClientFacade == null) {
            throw new NullPointerException("lobbyRequestClientFacade cannot be null");
        }

        this.lobbyClientHandlerSupplier = lobbyClientFacade;
        return this;
    }

    public GameClient withSessionClientHandlerSupplier(final Supplier<ChannelHandler<? extends PDU>> sessionClientFacade) {
        this.sessionClientHandlerSupplier = sessionClientFacade;
        return this;
    }

    public GameClient withStateClientHandlerSupplier(final Supplier<ChannelHandler<? extends PDU>> stateClientFacade) {
        this.stateClientHandlerSupplier = stateClientFacade;
        return this;
    }

    public GameClient withChatMessageClientHandlerSupplier(final Supplier<ChannelHandler<? extends PDU>> chatMessageClientFacade) {
        this.chatMessageClientHandlerSupplier = chatMessageClientFacade;
        return this;
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

    private Supplier<ChannelHandler<? extends PDU>> injectServerChannelIntoClientHandler(final Supplier<ChannelHandler<? extends PDU>> channelPDUCommunicationsHandlerSupplier) {
        final var channelPDUCommunicationsHandler = Objects.requireNonNull(channelPDUCommunicationsHandlerSupplier.get());

        Class<?> clazz = channelPDUCommunicationsHandler.getClass();

        while (Stream.of(clazz.getDeclaredFields()).map(Field::getType).noneMatch(Channel.class::equals)) {
            clazz = clazz.getSuperclass();
        }

        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(Channel.class))
                .findAny().ifPresent(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(channelPDUCommunicationsHandlerSupplier, serverChannel);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace(); // todo: log4j
                    }
                });

        return () -> channelPDUCommunicationsHandler;
    }
}
