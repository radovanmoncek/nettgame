package container.game.docker.ship.bootstrap;

import container.game.docker.ship.examples.creators.ServiceCreator;
import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import container.game.docker.ship.parents.products.Product;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
    private final LinkedList<Creator> channelGroupHandlerSuppliers;
    private final Map<Byte, Class<? extends ProtocolDataUnit>> protocolIdentifierToProtocolDataUnitBindings;
    private final Map<Class<? extends ProtocolDataUnit>, Byte> protocolDataUnitToProtocolIdentifierBindings;
    private Creator playerSessionDataCreator;

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
                    Port number must not belong to the interval [-Inf, 1024] U [65535, Inf], which is a reserved port pool, or be lesser than its lowest bound.
                """
            );

        this.port = port;

        return this;
    }

    public InstanceContainer withPlayerSessionDataCreator(final Creator playerSessionDataCreator) {

        this.playerSessionDataCreator = playerSessionDataCreator;

        return this;
    }

    public InstanceContainer withChannelGroupHandlerCreator(final Creator channelGroupHandlerCreator) {

        channelGroupHandlerSuppliers.add(new Creator() {

            @Override
            public Product newProduct() {

                final var channelGroupHandler = (ChannelGroupHandler<?, ?>) channelGroupHandlerCreator.newProduct();

                try {

                    final var playerSessionDataCreator = channelGroupHandler
                            .getClass()
                            .getSuperclass()
                            .getDeclaredField("playerSessionDataCreator");

                    playerSessionDataCreator.setAccessible(true);
                    playerSessionDataCreator.set(channelGroupHandler, InstanceContainer.this.playerSessionDataCreator);
                }
                catch (final Exception exception) {

                    logger.error(new Exception("Failed to inject ChannelGroupHandler, expect incorrect operation", exception));
                }

                return channelGroupHandler;
            }
        });

        return this;
    }

    public InstanceContainer withDecoderCreator(final Creator decoderCreator) {

        channelGroupHandlerSuppliers.add(new Creator(){

            @Override
            public Product newProduct() {

                final var decoder = (Decoder<?>) decoderCreator.newProduct();

                try {

                    final var bindingsField = decoder
                            .getClass()
                            .getSuperclass()
                            .getDeclaredField("protocolIdentifierToProtocolDataUnitBindings");

                    bindingsField.setAccessible(true);
                    bindingsField.set(decoder, protocolIdentifierToProtocolDataUnitBindings);
                }
                catch (final Exception exception) {

                    logger.error(new Exception("Unable to inject bindings, expect incorrect operation", exception));
                }

                return decoder;
            }
        });

        return this;
    }

    public InstanceContainer withEncoderCreator(final Creator encoderCreator) {

        channelGroupHandlerSuppliers.add(new Creator() {

            @Override
            public Product newProduct() {

                final Encoder<?> encoder = (Encoder<?>) encoderCreator.newProduct();

                try {

                    final var bindingsField = encoder
                            .getClass()
                            .getSuperclass()
                            .getDeclaredField("protocolDataUnitToProtocolIdentifierBindings");

                    bindingsField.setAccessible(true);
                    bindingsField.set(encoder, protocolDataUnitToProtocolIdentifierBindings);
                } catch (final Exception exception) {

                    logger.error(new Exception("Unable to inject bindings, expect incorrect operation", exception));
                }

                return encoder;
            }
        });

        return this;
    }

    public InstanceContainer withServiceCreator(final ServiceCreator serviceCreator) {

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
        final var bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
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

            serverChannel
                    .closeFuture()
                    .addListener(future -> shutdownGracefullyAfterNSeconds(shutdownSeconds));
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
