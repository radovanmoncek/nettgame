package container.game.docker.ship.bootstrap;

import container.game.docker.ship.injection.annotations.InjectDecoderBindings;
import container.game.docker.ship.injection.annotations.InjectEncoderBindings;
import container.game.docker.ship.injection.annotations.InjectService;
import container.game.docker.ship.injection.annotations.InjectSessionFactory;
import container.game.docker.ship.injection.exceptions.InjectionException;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.products.Product;
import container.game.docker.ship.parents.repositories.Repository;
import container.game.docker.ship.parents.services.Service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import java.net.InetAddress;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * <p>
 * A game and platform independent game server that is meant to run within a Docker container.
 * Multiple containers may be run in parallel on a single physical platform.
 * </p>
 * <p>
 * This class makes use of the <a href=https://refcatoring.guru>Singleton</a> design pattern,
 * meaning it may only be instantiated at most <b>once</b> per runtime.
 * </p>
 * <p>
 * The default port number is the port <b>4321</b>.
 * </p>
 * <p>
 * The address defaults to localhost / 127.0.0.1.
 * </p>
 */
public final class InstanceContainerBootstrap {
    private static final Logger logger = LogManager.getLogger(InstanceContainerBootstrap.class);
    /**
     * Singleton instance
     */
    private static InstanceContainerBootstrap instance;
    /**
     * The server port. Defaults to 4321.
     */
    private Integer port;
    /**
     * The address of this InstanceContainer. Defaults to localhost / 127.0.0.1.
     */
    private InetAddress address;
    private TimerTask gracefulShutdownTask;
    private final LinkedList<ChannelHandler> initialHandlers;
    private final LinkedList<Creator> channelHandlerCreators;
    private final LinkedList<Class<?>> persistableClasses;
    private final LinkedList<Service<?>> services;
    private SessionFactory sessionFactory;
    private final Map<Byte, Class<?>> magicBytesToFlatBufferSerializablesBindings;

    private InstanceContainerBootstrap() {

        channelHandlerCreators = new LinkedList<>();
        persistableClasses = new LinkedList<>();
        services = new LinkedList<>();
        initialHandlers = new LinkedList<>();
        magicBytesToFlatBufferSerializablesBindings = new HashMap<>();
        port = 4321;
        address = InetAddress.getLoopbackAddress();
    }

    public static InstanceContainerBootstrap newInstance() {

        return Objects.requireNonNullElse(instance, instance = new InstanceContainerBootstrap());
    }

    public void setPort(final Integer port) {

        if (port <= 1024 || port > 65535)
            throw new IllegalArgumentException(
                    """
                                Port number must not belong to the interval [-Inf, 1024] U [65535, Inf], which is a reserved port pool, or be lesser than its lowest bound.
                            """
            );

        this.port = port;
    }

    public void addPersistableClass(final Class<?> persistableClass) {

        persistableClasses.add(persistableClass);
    }

    public void addService(final Service<?> service) {

        services.add(service);
    }

    public void addChannelHandlerCreator(final Creator channelHandlerCreator) {

        channelHandlerCreators.add(channelHandlerCreator);
    }

    public void setLogLevel(final LogLevel logLevel) {

        initialHandlers.offer(new LoggingHandler(logLevel));
    }

    private LinkedList<Creator> returnInjectedChannelHandlerCreators() {

        logger.warn("Starting ChannelHandler injection");

        final var injectedChannelHandlerCreators = new LinkedList<Creator>();

        for (final var channelHandlerCreator : channelHandlerCreators) {

            injectedChannelHandlerCreators.add(new Creator() {

                @Override
                public Product newProduct() {

                    final var channelHandler = channelHandlerCreator.newProduct();

                    Class<?> clazz = channelHandler.getClass();

                    while (clazz.getSuperclass() != ChannelHandlerAdapter.class) {

                        final var declaredFields = clazz.getDeclaredFields();
                        final StringBuilder injectionExceptionReason = new StringBuilder("Failed to inject");

                        for (final var field : declaredFields) {

                            try {

                                /*if (field.isAnnotationPresent(container.game.docker.ship.injection.annotations.SessionFactory.GameSessionContextCreator.class)) {

                                    field.setAccessible(true);
                                    field.set(channelHandler, gameSessionContextCreator);

                                    injectionExceptionReason.append(String.format(" %s into %s", gameSessionContextCreator, field));

                                    logger.debug("Injecting {} into {}", gameSessionContextCreator, field);
                                }

                                if (field.isAnnotationPresent(container.game.docker.ship.injection.annotations.SessionFactory.PlayerSessionContextCreator.class)) {

                                    field.setAccessible(true);
                                    field.set(channelHandler, playerSessionContextCreator);

                                    injectionExceptionReason.append(String.format(" %s into %s", playerSessionContextCreator, field));

                                    logger.debug("Injecting {} into {}", playerSessionContextCreator, field);
                                }*/

                                if (field.isAnnotationPresent(InjectService.class)) {

                                    Service<?> serviceToInject = null;

                                    for (final var service : services) {

                                        if (service.getClass().equals(field.getType())) {

                                            serviceToInject = service;

                                            break;
                                        }
                                    }

                                    injectionExceptionReason.append(String.format(" %s into %s", channelHandler, field));

                                    if (serviceToInject == null) {

                                        throw new InjectionException(injectionExceptionReason.toString());
                                    }

                                    Class<?> serviceClazz = serviceToInject.getClass();

                                    while (serviceClazz.getSuperclass() != Service.class) {

                                        serviceClazz = serviceClazz.getSuperclass();
                                    }

                                    serviceClazz = serviceClazz.getSuperclass();

                                    for (final var serviceField : serviceClazz.getDeclaredFields()) {

                                        if (serviceField.getType().equals(Repository.class)) {

                                            final var repository = serviceField.get(serviceToInject);

                                            for (final var repositoryField : repository.getClass().getDeclaredFields()) {

                                                if (repositoryField.isAnnotationPresent(InjectSessionFactory.class)) {

                                                    repositoryField.setAccessible(true);
                                                    repositoryField.set(repository, sessionFactory);
                                                }
                                            }
                                        }
                                    }

                                    field.setAccessible(true);
                                    field.set(channelHandler, serviceToInject);

                                    logger.debug("Injecting {} into {}", channelHandler, field);
                                }

                                if(field.isAnnotationPresent(InjectDecoderBindings.class)) {

                                    field.setAccessible(true);
                                    field.set(channelHandler, magicBytesToFlatBufferSerializablesBindings);

                                    injectionExceptionReason.append(String.format(" %s into %s", channelHandler, field));

                                    logger.debug("Injecting {} into {}", channelHandler, field);
                                }

                                if(field.isAnnotationPresent(InjectEncoderBindings.class)) {

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

                    return channelHandler;
                }
            });
        }

        return injectedChannelHandlerCreators;
    }

    public void registerMagicByteToFlatBufferSerializableBinding(
            final Byte magicByte,
            final Class<?> flatBufferSerializable
    ) {

        if (magicBytesToFlatBufferSerializablesBindings.containsKey(magicByte)) {

            logger.warn("Binding of {} to {} already registered.", magicByte, magicBytesToFlatBufferSerializablesBindings.get(magicByte));

            return;
        }

        magicBytesToFlatBufferSerializablesBindings.put(magicByte, flatBufferSerializable);
    }

    private Map<Class<?>, Byte> invertMagicByteToFlatBufferSerializableBindings() {

        final var invertedMagicByteToFlatBufferSerializableBindings = new HashMap<Class<?>, Byte>();
        final BiConsumer<Byte, Class<?>> inversionBiConsumer =
                (magicByte, flatBufferSerializable) -> invertedMagicByteToFlatBufferSerializableBindings.put(flatBufferSerializable, magicByte);

        magicBytesToFlatBufferSerializablesBindings.forEach(inversionBiConsumer);

        return invertedMagicByteToFlatBufferSerializableBindings;
    }

    /**
     * <p>
     * Runs this {@link InstanceContainerBootstrap}, and does not block the current thread, meaning, it returns immediately after being called.
     * </p>
     *
     * @param shutdownSeconds the number of seconds before this {@link InstanceContainerBootstrap} is gracefully shutdown.
     */
    public void run(int shutdownSeconds) {

        final var bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new InstanceContainerChannelInitializer(initialHandlers, returnInjectedChannelHandlerCreators()))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try /*(
                final var registry = new StandardServiceRegistryBuilder().build();
                final var sessionFactory = new MetadataSources(registry)
                        .addAnnotatedClasses(persistableClasses.toArray(Class[]::new))
                        .buildMetadata()
                        .buildSessionFactory()
        )*/ {

            //this.sessionFactory = sessionFactory;

            final var serverChannel = bootstrap
                    .bind(port)
                    .sync()
                    .channel();

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
                    .addListener(future -> {

                        if (future.isSuccess()) {

                            logger.warn("GameServer stopped successfully.");
                        }

                        else {

                            logger.error("GameServer failed to stop", future.cause());
                        }

                        shutdownGracefullyAfterNSeconds(shutdownSeconds);
                    });
        } catch (final InterruptedException interruptedException) {

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

    public void setInternetProtocolAddress(InetAddress address) {

        this.address = address;
    }
}
