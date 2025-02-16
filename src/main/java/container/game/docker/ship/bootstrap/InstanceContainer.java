package container.game.docker.ship.bootstrap;

import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 *     A game and platform independent game server that is meant to run within a Docker container.
 *     Multiple containers may be run in parallel on a single physical platform.
 * </p>
 * <p>
 *     This class makes use of the <a href=https://refcatoring.guru>Singleton, and Builder</a> design patterns
 *     meaning it may only be instantiated at most once per runtime.
 * </p>
 * <p>
 *     The default port number is the port <b>4321</b>.
 * </p>
 */
public final class InstanceContainer {
    private static final Logger logger = LogManager.getLogger(InstanceContainer.class);
    /**
     * Singleton instance
     */
    private static InstanceContainer instance;
    /**
     * The server port. Defaults to 4321.
     */
    private Integer port;
    private TimerTask gracefulShutdownTask;
    private final LinkedList<Supplier<? extends ChannelHandler>> channelGroupHandlerSuppliers;
    private final Map<Byte, Class<? extends ProtocolDataUnit>> protocolIdentifierToProtocolDataUnitBindings;
    private final Map<Class<? extends ProtocolDataUnit>, Byte> protocolDataUnitToProtocolIdentifierBindings;

    private InstanceContainer() {

        channelGroupHandlerSuppliers = new LinkedList<>();
        protocolIdentifierToProtocolDataUnitBindings = new HashMap<>();
        protocolDataUnitToProtocolIdentifierBindings = new HashMap<>();
    }

    public static InstanceContainer newInstance() {

        if (instance == null)
            instance = new InstanceContainer()
                    .withPort(4321);

        return instance;
    }

    public InstanceContainer withPort(final Integer port) {

        if (port <= 1024 || port > 65535)
            throw new IllegalArgumentException(
                """
                    Port number must not belong to the interval [1, 1024], which is a reserved port pool, or be lesser than its lowest bound.
                """
            );

        this.port = port;

        return this;
    }

    public InstanceContainer withChannelGroupHandlerFactory(
            final Supplier<ChannelGroupHandler<? extends ProtocolDataUnit, ? extends ProtocolDataUnit>> channelGroupHandlerSupplier
    ) {
         channelGroupHandlerSuppliers.add(channelGroupHandlerSupplier);

        return this;
    }

    public InstanceContainer withDecoderFactory(final Function<Map<Byte, Class<? extends ProtocolDataUnit>>, Decoder<? extends ProtocolDataUnit>> decoderFactory) {

        channelGroupHandlerSuppliers.add(() -> decoderFactory.apply(protocolIdentifierToProtocolDataUnitBindings));

        return this;
    }

    public InstanceContainer withEncoderFactory(final Function<Map<Class<? extends ProtocolDataUnit>, Byte>, Encoder<? extends ProtocolDataUnit>> encoderFactory) {

        channelGroupHandlerSuppliers.add(() -> encoderFactory.apply(protocolDataUnitToProtocolIdentifierBindings));

        return this;
    }

    public InstanceContainer registerProtocolDataUnitIdentifierToProtocolDataUnitBinding(
            final Byte protocolIdentifier,
            final Class<? extends ProtocolDataUnit> protocolDataUnit
    ) {

        if (protocolIdentifierToProtocolDataUnitBindings.containsKey(protocolIdentifier)) {

            logger.error("Protocol identifier {} already registered.", protocolIdentifier);

            return this;
        }

        if (protocolIdentifier < ProtocolDataUnit.MIN_PROTOCOL_IDENTIFIER || ProtocolDataUnit.MAX_PROTOCOL_IDENTIFIER < protocolIdentifier) {

            logger.error("Protocol identifier {} is out of bounds.", protocolIdentifier);

            return this;
        }

        protocolIdentifierToProtocolDataUnitBindings.put(protocolIdentifier, protocolDataUnit);

        protocolDataUnitToProtocolIdentifierBindings.put(protocolDataUnit, protocolIdentifier);

        return this;
    }

    /**
     * <p>
     *     Runs this {@link InstanceContainer}, and does not block the current thread, meaning, it returns immediately after being called.
     * </p>
     *
     * @param shutdownSeconds the number of seconds before this {@link InstanceContainer} is gracefully shutdown.
     */
    public void run(int shutdownSeconds) {
        final var bossGroup = new NioEventLoopGroup();
        final var workerGroup = new NioEventLoopGroup();
        final var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new InstanceContainerChannelInitializer(channelGroupHandlerSuppliers))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {

            final var serverChannel = bootstrap.bind(port).sync().channel();

            logger.info("GameServer running on port {}", port);

            gracefulShutdownTask = new TimerTask() {

                @Override
                public void run() {

                    serverChannel.close();
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            };

            serverChannel.closeFuture().addListener(future -> shutdownGracefullyAfterNSeconds(shutdownSeconds));
        }
        catch (final InterruptedException interruptedException) {

            logger.error(interruptedException.getMessage(), interruptedException);
        }
    }

    public void run() {

        run(0);
    }

    public void shutdownGracefullyAfterNSeconds(final int seconds) {

        logger.warn("Shutting down gracefully after {} seconds", seconds);

        new Timer()
                .schedule(gracefulShutdownTask, (long) seconds * 1000);
    }
}
