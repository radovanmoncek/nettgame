package cz.radovanmoncek.ship.bootstrap;

import cz.radovanmoncek.ship.injection.services.ChannelHandlerInjectionService;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.creators.RepositoryCreator;
import cz.radovanmoncek.ship.parents.models.PersistableModel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * This game server is build as an extension of the Netty Java framework providing potential
 * implementors with useful features for networked game development.
 * </p>
 * <p>
 * This class should not be instantiated directly, for that, please refer to: {@link cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder}
 * and {@link cz.radovanmoncek.ship.directors.GameServerBootstrapDirector}.
 * </p>
 * <p>
 * The default port number is the port <b>4321</b>.
 * </p>
 * <p>
 * The address defaults to localhost / 127.0.0.1.
 * </p>
 *
 * @author Radovan Monček
 * @apiNote <p>
 * The <a href=https://refcatoring.guru>Singleton</a> design pattern has been made use of,
 * meaning this may only be instantiated, at most, <b>once</b> per runtime.
 * </p>
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
    /**
     * General {@link ChannelHandler}s that will be added to the front of the {@link ChannelPipeline}.
     */
    private final LinkedList<ChannelHandler> initialHandlers;
    /**
     * Creators of the "actual" business logic handlers.
     */
    private final LinkedList<ChannelHandlerCreator> channelHandlerCreators;
    /**
     * Data storage solution that is automatically injected into business logic handlers.
     */
    private final LinkedList<PersistableModel> persistableModels;
    /**
     * Creator of the specific repository soluttion that is currently in use.
     */
    private RepositoryCreator repositoryCreator;
    /**
     * The number of seconds before this {@link GameServerBootstrap} is gracefully shutdown.
     */
    private int shutdownTimeout;

    private GameServerBootstrap() {

        channelHandlerCreators = new LinkedList<>();
        persistableModels = new LinkedList<>();
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

    public void setRepositoryCreator(RepositoryCreator repositoryCreator) {

        this.repositoryCreator = repositoryCreator;
    }

    public void addPersistableModel(final PersistableModel persistableModel) {

        persistableModels.add(persistableModel);
    }

    public void addChannelHandlerCreator(final ChannelHandlerCreator channelHandlerCreator) {

        channelHandlerCreators.add(channelHandlerCreator);
    }

    public void setLogLevel(final LogLevel logLevel) {

        initialHandlers.offer(new LoggingHandler(logLevel));
    }

    public void run0() {

        final var bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        final var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try (
                final var registry = new StandardServiceRegistryBuilder().build();
                final var sessionFactory = new MetadataSources(registry)
                        .addAnnotatedClasses(persistableModels.stream().map(Object::getClass).toArray(Class[]::new))
                        .buildMetadata()
                        .buildSessionFactory()
        ) {

            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) {

                    initialHandlers.forEach(ch.pipeline()::addLast);

                    new ChannelHandlerInjectionService(
                            sessionFactory,
                            channelHandlerCreators,
                            repositoryCreator,
                            persistableModels
                    )
                            .returnInjectedChannelHandlerCreators()
                            .stream()
                            .map(ChannelHandlerCreator::newProduct)
                            .forEach(ch.pipeline()::addLast);
                }
            });


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

            while (!sessionFactory.isClosed()) {

                try {

                    logger.debug("SessionFactory heart beat");

                    TimeUnit.MINUTES.sleep(1);
                } catch (final InterruptedException interruptedException) {

                    logger.error(interruptedException.getMessage(), interruptedException);
                }
            }
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

    /**
     * <p>
     * Runs this {@link GameServerBootstrap}, and does not block the current thread, meaning, it returns immediately after being called.
     * </p>
     */
    public void run() {

        final var keepAliveSessionFactoryRunnable = (Runnable) this::run0;
        final var keepAliveSessionFactoryThread = Executors
                .defaultThreadFactory()
                .newThread(keepAliveSessionFactoryRunnable);

        keepAliveSessionFactoryThread.setName("SessionFactory heart beat");
        keepAliveSessionFactoryThread.start();
    }
}
