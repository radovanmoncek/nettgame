package cz.radovanmoncek.client.ship.bootstrap;

import cz.radovanmoncek.ship.injection.annotations.InjectDecoderBindings;
import cz.radovanmoncek.ship.injection.annotations.InjectEncoderBindings;
import cz.radovanmoncek.ship.injection.exceptions.InjectionException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * <p>
 * The Docker Game Client object enabling networked socket communication with the Docker Game Server.
 * </p>
 * <p>
 * This class uses the <a href=https://refactoring.guru>Singleton and Builder Design Patterns</a>, since it is bound to a Socket.
 * There can exist at most one instance of this class per runtime.
 * </p>
 */
public final class GameClientBootstrap {
    private static final Logger logger = LogManager.getLogger(GameClientBootstrap.class);
    /**
     * Singleton instance
     */
    private static GameClientBootstrap instance;
    private int gameServerPort;
    private InetAddress gameServerAddress;
    private final LinkedList<ChannelHandler> channelHandlers;
    private final Map<Byte, Class<?>> magicByteToFlatBuffersSchemaBindings;
    private TimerTask gracefulShutdownTimerTask;
    private boolean shutdownOnDisconnect;

    private GameClientBootstrap() {

        channelHandlers = new LinkedList<>();
        magicByteToFlatBuffersSchemaBindings = new HashMap<>();
        shutdownOnDisconnect = true;
    }

    public void setShutdownOnDisconnect(boolean shutdownOnDisconnect) {

        this.shutdownOnDisconnect = shutdownOnDisconnect;
    }

    /**
     * @param reconnectIntervalSeconds   the number of seconds between reconnect attempts.
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
                    .handler(new GameClientChannelInitializer(returnInjectedChannelHandlerCreators()));

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
                } catch (final Exception exception) {

                    logger.error(exception.getMessage(), exception);
                }
            }

            if (serverChannel == null)
                throw new Exception("Failed to connect to the instance container");

            serverChannel
                    .closeFuture()
                    .addListener(future -> {

                if (!future.isSuccess()) {

                    logger.error(future.cause().getMessage(), future.cause());

                    return;
                }

                logger.info("Successfully disconnected from the instance container");

                if(shutdownOnDisconnect)
                    shutdownGracefully();
            });
        } catch (final Exception exception) {

            logger.error(exception.getMessage(), exception);

            workerGroup.shutdownGracefully();
        }
    }

    public static GameClientBootstrap newInstance() {

        if (Objects.isNull(instance))
            instance = new GameClientBootstrap();

        instance.setServerPort(4321);
        instance.setInstanceContainerAddress(InetAddress.getLoopbackAddress());

        return instance;
    }

    public void setServerPort(final int serverPort) {

        if (serverPort <= 1024 || serverPort > 65535)
            throw new IllegalArgumentException("Server port must be between 1024 and 65535");

        this.gameServerPort = serverPort;
    }

    public void setInstanceContainerAddress(final InetAddress gameServerAddress) {

        this.gameServerAddress = gameServerAddress;
    }

    private LinkedList<ChannelHandler> returnInjectedChannelHandlerCreators() {

        final var injectedChannelHandlers = new LinkedList<ChannelHandler>();

        for (final var channelHandler : channelHandlers) {

            Class<?> clazz = channelHandler.getClass();

            while (clazz != null && clazz.getSuperclass() != ChannelHandler.class) {

                final var declaredFields = clazz.getDeclaredFields();
                final StringBuilder injectionExceptionReason = new StringBuilder("Failed to inject");

                for (final var field : declaredFields) {

                    try {

                        if (field.isAnnotationPresent(InjectDecoderBindings.class)) {

                            field.setAccessible(true);
                            field.set(channelHandler, magicByteToFlatBuffersSchemaBindings);

                            injectionExceptionReason.append(String.format(" %s into %s", channelHandler, field));

                            logger.debug("Injecting {} into {}", channelHandler, field);
                        }

                        if (field.isAnnotationPresent(InjectEncoderBindings.class)) {

                            field.setAccessible(true);
                            field.set(channelHandler, invertMagicByteToFlatBufferSerializableBindings());

                            injectionExceptionReason.append(String.format(" %s into %s", channelHandler, field));

                            logger.debug("Injecting {} into {}", channelHandler, field);
                        }
                    } catch (final Exception exception) {

                        logger.error(new InjectionException(injectionExceptionReason.append(", expect incorrect operation!").toString(), exception));

                        break;
                    }
                }

                clazz = clazz.getSuperclass();
            }

            injectedChannelHandlers.add(channelHandler);
        }

        return injectedChannelHandlers;
    }

    private Map<Class<?>, Byte> invertMagicByteToFlatBufferSerializableBindings() {

        final var invertedMagicByteToFlatBufferSerializableBindings = new HashMap<Class<?>, Byte>();
        final BiConsumer<Byte, Class<?>> inversionBiConsumer =
                (magicByte, flatBufferSerializable) -> invertedMagicByteToFlatBufferSerializableBindings.put(flatBufferSerializable, magicByte);

        magicByteToFlatBuffersSchemaBindings.forEach(inversionBiConsumer);

        return invertedMagicByteToFlatBufferSerializableBindings;
    }

    public void addChannelHandler(final ChannelHandler channelHandler) {

        channelHandlers.add(channelHandler);
    }

    public void registerProtocolDataUnitToProtocolDataUnitBinding(
            final Byte magicByte,
            final Class<?> flatBuffersSchema
    ) {

        magicByteToFlatBuffersSchemaBindings.put(magicByte, flatBuffersSchema);
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

    public void setLogLevel(LogLevel logLevel) {

        channelHandlers.offer(new LoggingHandler(logLevel));
    }
}
