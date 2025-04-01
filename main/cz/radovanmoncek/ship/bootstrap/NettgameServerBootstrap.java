package cz.radovanmoncek.ship.bootstrap;

import cz.radovanmoncek.ship.bootstrap.exception.BootstrapInvalidException;
import cz.radovanmoncek.ship.builders.NettgameServerBootstrapBuilder;
import cz.radovanmoncek.ship.directors.NettgameServerBootstrapDirector;
import cz.radovanmoncek.ship.injection.annotations.ChannelHandlerAttributeInjectee;
import cz.radovanmoncek.ship.injection.services.ChannelHandlerInjectionService;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.repositories.Repository;
import cz.radovanmoncek.ship.utilities.logging.LoggingUtilities;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.lang.reflect.ParameterizedType;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * A game and platform independent game server that is meant to run within a Docker container.
 * </p>
 * <p>
 * This game server is build as an extension of the Netty Java framework providing potential
 * implementors with useful features for networked game development.
 * </p>
 * <p>
 * This class should not be instantiated directly, for that, please refer to: {@link NettgameServerBootstrapBuilder}
 * and {@link NettgameServerBootstrapDirector}.
 * </p>
 * <p>
 * The default port number is the port <b>4321</b>.
 * </p>
 * <p>
 * The address defaults to localhost / 127.0.0.1.
 * </p>
 * <p>
 *     All added {@link ChannelHandlerCreator}s are automatically attribute injected with required dependencies.
 *     For more information, please refer to: {@link ChannelHandlerAttributeInjectee}.
 * </p>
 *
 * @author Radovan Monček
 * @apiNote <p>
 * The <a href=https://refcatoring.guru>Singleton</a> design pattern has been made use of,
 * meaning this may only be instantiated, at most, <b>once</b> per runtime.
 * </p>
 * The order in which some of the methods in this class are called matters, please refer to the method documentation for more insight.
 * @since 1.0
 */
public final class NettgameServerBootstrap {
    /**
     * Logger instance.
     */
    private static final Logger logger = Logger.getLogger(NettgameServerBootstrap.class.getName());
    /**
     * Singleton instance
     */
    private static NettgameServerBootstrap instance;
    /**
     * Defaults to 4321.
     */
    private Integer port;
    /**
     * Defaults to localhost / 127.0.0.1.
     */
    private InetAddress address;
    /**
     * General {@link ChannelHandler}s that will be added to the head of the {@link ChannelPipeline}.
     */
    private final LinkedList<ChannelHandler> initialHandlers;
    /**
     * Creators of the "actual" business logic handlers.
     */
    private final LinkedList<ChannelHandlerCreator> channelHandlerCreators;
    /**
     * Data storage solution that is automatically injected into business logic handlers.
     */
    private final LinkedList<Repository<?>> repositories;
    /**
     * The number of seconds before this {@link NettgameServerBootstrap} is gracefully shutdown.
     */
    private int shutdownTimeout;

    //Thank you: https://stackoverflow.com/questions/6315699/why-are-the-level-fine-logging-messages-not-showing
    static {

        LoggingUtilities.enableGlobalLoggingLevel(Level.parse(System.getenv("LOG_LEVEL")));
    }

    private NettgameServerBootstrap() {

        channelHandlerCreators = new LinkedList<>();
        repositories = new LinkedList<>();
        initialHandlers = new LinkedList<>();
        port = 4321;
        address = InetAddress.getLoopbackAddress();
    }

    /**
     * Return a singleton instance (at most one can exist per runtime) of this.
     *
     * @return singleton of {@code this}.
     */
    public static NettgameServerBootstrap returnNewInstance() {

        return Objects.requireNonNullElse(instance, instance = new NettgameServerBootstrap());
    }

    /**
     * Set a server port.
     * Port must conform to the bounds of interval: [1025, 65534].
     *
     * @param port the port number to set.
     */
    public void setPort(final Integer port) {

        if (port <= 1024 || port > 65535)
            throw new IllegalArgumentException(
                    """
                            Port number must not belong to the interval (-Inf, 1024) U (65535, Inf), which is a reserved port pool, or be lesser than its lowest bound.
                            """
            );

        this.port = port;
    }

    /**
     * Add a {@link Repository} to be injected to business logic handlers.
     * Order does not matter.
     *
     * @param repository the PersistableModel to append.
     */
    public void addRepository(final Repository<?> repository) {

        repositories.add(repository);
    }

    /**
     * <p>
     * Adds a given {@link ChannelHandlerCreator} to the handler list for initialization.
     * </p>
     * <b style="color:red;">
     * It is of upmost importance that the implementor know that, the <i>order</i> in which this method is called,
     * and thus {@link ChannelHandlerCreator}s are added, <i>matters</i>!
     * </b>
     *
     * @param channelHandlerCreator the {@link ChannelHandlerCreator} to append to the initialization list.
     */
    public void addChannelHandlerCreator(final ChannelHandlerCreator channelHandlerCreator) {

        channelHandlerCreators.add(channelHandlerCreator);
    }

    /**
     * Set a Netty LogLevel handler for this.
     *
     * @param logLevel the LogLevel to set.
     */
    public void setLogLevel(final LogLevel logLevel) {

        initialHandlers.offer(new LoggingHandler(logLevel));
    }

    private void run0() {

        logger.fine("\nBootstrapping -> Initializing Hibernate ...");

        try (
                final var sessionFactory = new Configuration()
                                .setProperty("hibernate.connection.url", Boolean.parseBoolean(System.getProperty("containerized")) ? System.getenv("DATABASE_URL") : System.getenv("LOCAL_DATABASE_URL")) //todo: only use argument
                                .setProperty("hibernate.connection.username", System.getenv("HIBERNATE_DB_USER"))
                                .setProperty("hibernate.connection.password", System.getenv("MYSQL_ROOT_PASSWORD"))
                                .addAnnotatedClasses(
                                        repositories
                                                .stream()
                                                .map(repository -> ReflectionUtilities
                                                        .returnTypeParameterAtIndex((ParameterizedType) repository.getClass().getGenericSuperclass(), 0)
                                                )
                                                .toArray(Class[]::new)
                                )
                                .buildSessionFactory();
        ) {

            final var bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            final var workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

            logger.fine("\nBootstrapping -> Initializing Netty ...");

            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {

                            LoggingUtilities.enableGlobalLoggingLevel(Level.ALL);

                            initialHandlers.forEach(ch.pipeline()::addLast);

                            new ChannelHandlerInjectionService(sessionFactory, repositories)
                                    .returnInjectedChannelHandlerCreators(channelHandlerCreators)
                                    .stream()
                                    .map(ChannelHandlerCreator::newProduct)
                                    .forEach(ch.pipeline()::addLast);
                        }
                    })
                    .bind(address, port)
                    .sync()
                    .addListener((ChannelFutureListener) future -> {

                        if (future.isSuccess()) {

                            logger.log(Level.FINE, "Success -> Nettgame server is running @ {0}:{1}", new Object[]{address, port.toString()});

                            future
                                    .channel()
                                    .closeFuture()
                                    .addListener(closeFuture -> {

                                        if (future.isSuccess()) {

                                            logger.log(Level.WARNING, "Graceful shutdown in {0} seconds", shutdownTimeout);

                                            TimeUnit.SECONDS.sleep(shutdownTimeout);

                                            bossGroup.shutdownGracefully();
                                            workerGroup.shutdownGracefully();
                                            sessionFactory.close();

                                            logger.warning("GameServer stopped successfully.");
                                        } else {

                                            logger.log(Level.SEVERE, "GameServer failed to stop", future.cause());
                                        }
                                    });

                            return;
                        }

                        logger.log(Level.SEVERE, future.cause().getMessage(), future.cause());
                    });

            while (!sessionFactory.isClosed()) {

                try {

                    logger.finest("Running -> SessionFactory heart beat");

                    TimeUnit.MINUTES.sleep(1);
                } catch (final InterruptedException interruptedException) {

                    logger.log(Level.SEVERE, interruptedException.getMessage(), interruptedException);
                }
            }
        }
        catch (final InterruptedException interruptedException) {

            logger.log(Level.SEVERE, interruptedException.getMessage(), interruptedException);
        }
    }

    public void setInternetProtocolAddress(InetAddress address) {

        this.address = address;
    }

    public void setShutdownTimeout(int shutdownTimeout) {

        this.shutdownTimeout = shutdownTimeout;
    }

    /**
     * <p>
     *     Runs this {@link NettgameServerBootstrap}, and does not block the current thread, meaning, it returns immediately after being called.
     * </p>
     * @throws BootstrapInvalidException if the attribute state of {@code this} is invalid.
     * @apiNote § this violates the "exceptions must not be used as method termination mechanisms" guideline.
     * But an exception can be made, since this method is a prerequisite to the running state of the game server.
     */
    public void run() throws BootstrapInvalidException {

        if(!validate()) {

            throw new BootstrapInvalidException("Bootstrap failed to validate");
        }

        final var keepAliveSessionFactoryRunnable = (Runnable) this::run0;
        final var keepAliveSessionFactoryThread = Executors
                .defaultThreadFactory()
                .newThread(keepAliveSessionFactoryRunnable);

        keepAliveSessionFactoryThread.setName("SessionFactory heart beat");
        keepAliveSessionFactoryThread.start();
    }

    private boolean validate() {

        return port != 0 && address != null;
    }
}
