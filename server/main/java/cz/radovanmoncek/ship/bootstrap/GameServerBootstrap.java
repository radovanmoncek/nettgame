package cz.radovanmoncek.ship.bootstrap;

import cz.radovanmoncek.ship.injection.annotations.AttributeInjectee;
import cz.radovanmoncek.ship.injection.exceptions.InjectionException;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.repositories.Repository;
import cz.radovanmoncek.ship.parents.services.Service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A game and platform independent game server that is meant to run within a Docker container.
 * </p>
 * <p>
 *     This game server is build as an extension of the Netty Java framework providing potential
 *     implementors with useful features for networked game development.
 * </p>
 * <p>
 *     This class should not be instantiated directly, for that, please refer to: {@link cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder}
 *     and {@link cz.radovanmoncek.ship.directors.GameServerBootstrapDirector}.
 * </p>
 * <p>
 * The default port number is the port <b>4321</b>.
 * </p>
 * <p>
 * The address defaults to localhost / 127.0.0.1.
 * </p>
 * @apiNote <p>
 *     The <a href=https://refcatoring.guru>Singleton</a> design pattern has been made use of,
 *     meaning this may only be instantiated, at most, <b>once</b> per runtime.
 * </p>
 * @author Radovan Monček
 * @since 1.0
 */
public final class GameServerBootstrap {
    private static final Logger logger = LogManager.getLogger(GameServerBootstrap.class);
    /**
     * Singleton instance
     */
    private static GameServerBootstrap instance;
    /**
     * Defaults to 4321.
     */
    private Integer port;
    /**
     * Defaults to localhost / 127.0.0.1.
     */
    private InetAddress address;
    private final LinkedList<ChannelHandler> initialHandlers;
    private final LinkedList<ChannelHandlerCreator> channelHandlerCreators;
    private final LinkedList<Class<?>> persistableClasses;
    private final LinkedList<Service<?>> services;
    private SessionFactory sessionFactory;
    /**
     * The number of seconds before this {@link GameServerBootstrap} is gracefully shutdown.
     */
    private int shutdownTimeout;

    private GameServerBootstrap() {

        channelHandlerCreators = new LinkedList<>();
        persistableClasses = new LinkedList<>();
        services = new LinkedList<>();
        initialHandlers = new LinkedList<>();
        port = 4321;
        address = InetAddress.getLoopbackAddress();
    }

    public static GameServerBootstrap returnNewInstance() {

        return Objects.requireNonNullElse(instance, instance = new GameServerBootstrap());
    }

    public void setPort(final Integer port) {

        if (port <= 1024 || port > 65535)
            throw new IllegalArgumentException(
                    """
                            Port number must not belong to the interval (-Inf, 1024] U [65535, Inf), which is a reserved port pool, or be lesser than its lowest bound.
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

    public void addChannelHandlerCreator(final ChannelHandlerCreator channelHandlerCreator) {

        channelHandlerCreators.add(channelHandlerCreator);
    }

    public void setLogLevel(final LogLevel logLevel) {

        initialHandlers.offer(new LoggingHandler(logLevel));
    }

    private LinkedList<ChannelHandlerCreator> returnInjectedChannelHandlerCreators() {

        logger.warn("Starting ChannelHandler injection");

        final var injectedChannelHandlerCreators = new LinkedList<ChannelHandlerCreator>();

        for (final var channelHandlerCreator : channelHandlerCreators) {

            injectedChannelHandlerCreators.add(new ChannelHandlerCreator() {

                @Override
                public ChannelHandler newProduct() {

                    final var channelHandler = channelHandlerCreator.newProduct();

                    Class<?> clazz = channelHandler.getClass();

                    while (clazz.getSuperclass() != ChannelHandlerAdapter.class) {

                        final var declaredFields = clazz.getDeclaredFields();
                        final StringBuilder injectionExceptionReason = new StringBuilder("Failed to inject");

                        for (final var field : declaredFields) {

                            try {

                                if (field.isAnnotationPresent(AttributeInjectee.class)) {

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

                                                if (repositoryField.isAnnotationPresent(AttributeInjectee.class)) {

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

    /**
     * <p>
     * Runs this {@link GameServerBootstrap}, and does not block the current thread, meaning, it returns immediately after being called.
     * </p>
     */
    public void run() {

        final var bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) {

                initialHandlers.forEach(ch.pipeline()::addLast);

                returnInjectedChannelHandlerCreators()
                        .stream()
                        .map(ChannelHandlerCreator::newProduct)
                        .forEach(ch.pipeline()::addLast);
            }
        });

        try (
                final var registry = new StandardServiceRegistryBuilder().build();
                final var sessionFactory = new MetadataSources(registry)
                        .addAnnotatedClasses(persistableClasses.toArray(Class[]::new))
                        .buildMetadata()
                        .buildSessionFactory()
        ) {

            this.sessionFactory = sessionFactory;

            final var keepAliveSessionFactoryRunnable = (Runnable) () -> {

                while (!sessionFactory.isClosed()) {

                    try {

                        logger.debug("SessionFactory heart beat");

                        TimeUnit.MINUTES.sleep(1);
                    } catch (final InterruptedException interruptedException) {

                        logger.error(interruptedException.getMessage(), interruptedException);
                    }
                }
            };

            final var keepAliveSessionFactoryThread = Executors
                    .defaultThreadFactory()
                    .newThread(keepAliveSessionFactoryRunnable);

            keepAliveSessionFactoryThread.setName("SessionFactory heart beat");
            keepAliveSessionFactoryThread.start();

            bootstrap
                    .bind(address, port)
                    .sync()
                    .addListener((ChannelFutureListener) future -> {

                        if (future.isSuccess()) {

                            logger.info("GameServer running on port {}", port);

                            future
                                    .channel()
                                    .closeFuture()
                                    .addListener(closeFuture -> {

                                        if (future.isSuccess()) {

                                            logger.warn("Graceful shutdown in {} seconds", shutdownTimeout);

                                            TimeUnit.SECONDS.sleep(shutdownTimeout);

                                            bossGroup.shutdownGracefully();
                                            workerGroup.shutdownGracefully();
                                            sessionFactory.close();

                                            logger.warn("GameServer stopped successfully.");
                                        } else {

                                            logger.error("GameServer failed to stop", future.cause());
                                        }
                                    });

                            return;
                        }

                        logger.error(future.cause().getMessage(), future.cause());
                    });

        } catch (final InterruptedException interruptedException) {

            logger.error(interruptedException.getMessage(), interruptedException);
        }
    }

    public void setInternetProtocolAddress(InetAddress address) {

        this.address = address;
    }

    public void setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }
}
