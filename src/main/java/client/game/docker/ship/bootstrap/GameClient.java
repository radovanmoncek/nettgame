package client.game.docker.ship.bootstrap;

import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandle;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     The Docker Game Client object enabling networked socket communication with the Docker Game Server.
 * </p>
 * <p>
 *     This class uses the <a href=https://refactoring.guru>Singleton and Builder Design Patterns</a>, since it is bound to a Socket.
 *     There can exist at most one instance of this class per runtime.
 * </p>
 */
public final class GameClient {
    private static final Logger logger = LogManager.getLogger(GameClient.class);
    /**
     * Singleton instance
     */
    private static GameClient instance;
    private int gameServerPort;
    private InetAddress gameServerAddress;
    private final LinkedList<ChannelHandler> channelHandlers;
    private final Map<Byte, Class<? extends ProtocolDataUnit>> protocolIdentifierToProtocolDataUnitBindings;
    private final Map<Class<? extends ProtocolDataUnit>, Byte> protocolDataUnitToProtocolIdentifierBindings;
    private TimerTask gracefulShutdownTimerTask;

    private GameClient() {

        channelHandlers = new LinkedList<>(List.of(new LoggingHandler(LogLevel.INFO)));
        protocolIdentifierToProtocolDataUnitBindings = new HashMap<>();
        protocolDataUnitToProtocolIdentifierBindings = new HashMap<>();
    }

    /**
     * @param reconnectIntervalSeconds the number of seconds between reconnect attempts.
     * @param failReconnectAfterAttempts the number of reconnect retries.
     */
    public void run(int reconnectIntervalSeconds, int failReconnectAfterAttempts) {

        final var bootstrap = new Bootstrap();
        final var workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {

            bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new GameClientChannelInitializer(channelHandlers));

            Channel serverChannel = null;

            for (int i = 0; i < failReconnectAfterAttempts && serverChannel == null; i++) {

                try {

                    serverChannel = bootstrap
                            .connect(gameServerAddress, gameServerPort)
                            .sync()
                            .channel();

                    final var serverChannelFinal = serverChannel;

                    gracefulShutdownTimerTask = new TimerTask() {

                        @Override
                        public void run() {

                            serverChannelFinal.close();
                            workerGroup.shutdownGracefully();
                        }
                    };

                    TimeUnit.SECONDS.sleep(reconnectIntervalSeconds);
                }
                catch (final Exception exception) {

                    logger.error(exception.getMessage(), exception);
                }
            }

            if(serverChannel == null)
                throw new Exception("Failed to connect to the instance container");

            serverChannel.closeFuture().addListener(future -> {

                if(future.isSuccess())
                    logger.info("Successfully disconnected from the instance container");

                if(!future.isSuccess())
                    logger.error(future.cause().getMessage(), future.cause());

                shutdownGracefully();
            });
        }
        catch (final Exception exception) {

            logger.error(exception.getMessage(), exception);

            workerGroup.shutdownGracefully();
        }
    }

    public static GameClient newInstance() {

        if (Objects.isNull(instance))
            instance = new GameClient()
                    .withServerPort(4321)
                    .withInstanceContainerAddress(InetAddress.getLoopbackAddress());

        return instance;
    }

    public GameClient withServerPort(final int serverPort) {

        if(serverPort <= 1024 || serverPort > 65535)
            throw new IllegalArgumentException("Server port must be between 1024 and 65535");

        this.gameServerPort = serverPort;

        return this;
    }

    public GameClient withInstanceContainerAddress(final InetAddress gameServerAddress) {

        this.gameServerAddress = gameServerAddress;

        return this;
    }

    public GameClient withChannelHandler(final ChannelHandler channelHandler) {

        channelHandlers.add(channelHandler);

        return this;
    }

    public GameClient withEncoder(final Encoder<? extends ProtocolDataUnit> encoder) {

        try {

            final var encoderClass = encoder.getClass().isAnonymousClass() ? encoder.getClass().getSuperclass().getSuperclass() : encoder.getClass().getSuperclass();

            final var bindingsField = encoderClass.getDeclaredField("protocolDataUnitToProtocolIdentifierBindings");

            bindingsField.setAccessible(true);
            bindingsField.set(encoder, protocolDataUnitToProtocolIdentifierBindings);
        } catch (final Exception exception) {

            logger.error(new Exception("Failed to inject bindings into encoder, expect incorrect operation", exception));
        }

        channelHandlers.add(encoder);

        return this;
    }

    public GameClient withDecoder(final Decoder<? extends ProtocolDataUnit> decoder) {

        try{

            final var decoderClass = decoder.getClass().isAnonymousClass() ? decoder.getClass().getSuperclass().getSuperclass() : decoder.getClass().getSuperclass();
            final var bindingsField = decoderClass.getDeclaredField("protocolIdentifierToProtocolDataUnitBindings");

            bindingsField.setAccessible(true);
            bindingsField.set(decoder, protocolIdentifierToProtocolDataUnitBindings);
        }
        catch (final Exception exception) {

            logger.error(new Exception("Failed to inject bindings into decoder, expect incorrect operation", exception));
        }

        channelHandlers.add(decoder);

        return this;
    }

    public GameClient registerProtocolDataUnitToProtocolDataUnitBinding(
            final Byte protocolIdentifier,
            final Class<? extends ProtocolDataUnit> protocolDataUnit
    ) {

        protocolIdentifierToProtocolDataUnitBindings.put(protocolIdentifier, protocolDataUnit);

        protocolDataUnitToProtocolIdentifierBindings.put(protocolDataUnit, protocolIdentifier);

        return this;
    }

    public void shutdownGracefullyAfterNSeconds(final int seconds) {

        logger.info("Shutting down gracefully after {} seconds", seconds);

        new Timer()
                .schedule(gracefulShutdownTimerTask, (long) seconds * 1000);
    }

    public void run() {

        run(0, 10);
    }

    public void shutdownGracefully() {

        shutdownGracefullyAfterNSeconds(0);
    }
}
