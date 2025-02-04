package container.game.docker.ship.bootstrap;

import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p>
 * This class represents an instance of a Docker container running on top of the Netty framework, and containing several game instances.
 * It is possible to think of this class as a self contained "mini server" that is controlled and reports to the main Docker Game Server host instance.
 * </p>
 * <p>
 * This class makes use of the <a href=https://refcatoring.guru>Singleton, and Builder</a> design patterns meaning it may only be instantiated at most once per runtime.
 * </p>
 */
public final class InstanceContainer {
    /**
     * Singleton pattern
     */
    private static InstanceContainer instance;
    /**
     * The server port. Defaults to 4321.
     */
    private Integer port;
    /**
     * All the client channels connected to this server
     */
    private final ChannelGroup managedClients;
    private final ThreadGroup managedSessions;
    private ChannelFuture future;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private LinkedList<Supplier<? extends ChannelGroupHandler<? extends ProtocolDataUnit>>> channelGroupHandlerSuppliers;

    private InstanceContainer() {
        managedSessions = new ThreadGroup("Managed Sessions");
        managedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        port = 4321;
    }

    public static InstanceContainer newInstance() {
        if (instance == null)
            instance = new InstanceContainer();

        return instance;
    }

    public InstanceContainer withPort(final Integer port) {
        if (port <= 1024)
            throw new IllegalArgumentException(
                """
                    Port number must not belong to the interval [1, 1024], which is a reserved port pool, or be lesser than its lowest bound.
                """
            );

        this.port = port;

        return this;
    }

    public InstanceContainer withChannelGroupHandlerSupplier(
            final Supplier<? extends ChannelGroupHandler<? extends ProtocolDataUnit>> channelGroupHandlerSupplier
    ) {
         channelGroupHandlerSuppliers.add(() -> {
            final var channelGroupHandler = channelGroupHandlerSupplier.get();

            injectChannelGroupHandlerDependencies(channelGroupHandler);

            return channelGroupHandler;
        });

        return this;
    }

    /**
     * <p>
     * Runs the DockerGameServer instance and blocks the current thread until the {@link #shutdownGracefullyAfterNSeconds shutdownGracefullyAfterNSeconds(int seconds)} method is called.
     * </p>
     *
     * @throws InterruptedException if the {@link ChannelFuture#sync()} is interrupted
     */
    public void run(int shutdownSeconds) throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        final var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new InstanceContainerChannelInitializer(
                ))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            future = bootstrap.bind(port).sync();
            System.out.printf("GameServer running on port %d\n", port); //todo: log4j

            future.channel().closeFuture().sync();
        } finally {
            shutdownGracefullyAfterNSeconds(shutdownSeconds);
        }
    }

    public void run() throws InterruptedException {
        run(0);
    }

    public void shutdownGracefullyAfterNSeconds(final int seconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                future.channel().close();
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }, (long) seconds * 1000);
    }

    private void injectChannelGroupHandlerDependencies(ChannelGroupHandler<? extends ProtocolDataUnit> channelGroupHandler) {
        Class<?> clazz = channelGroupHandler.getClass();
        while (Stream.of(clazz.getDeclaredFields()).map(Field::getType).noneMatch(ChannelGroup.class::equals)) {
            clazz = clazz.getSuperclass();
        }

        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(ChannelGroup.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(channelGroupHandler, managedClients);
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace(); //todo: log4j
                    }
                });
    }
}
