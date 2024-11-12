package server.game.docker;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.modules.lobby.facades.LobbyServerFacade;
import server.game.docker.ship.parents.facades.ServerFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.stream.Stream;

/**
 * <p>
 * The root class of the DockerGameServer, it initializes and encapsulates the Netty protocol server and its functionality.
 * </p>
 * <p>
 * The default port number of the DockerGameServer is TCP 4321.
 * This value may be changed, please refer to {@link #withPort(Integer port)} for further information.
 * </p>
 * <p>
 * Please make sure to note that because of the multi-threaded nature of {@link EventLoopGroup} workers,
 * any methods utilizing their functionality are declared as synchronized.
 * </p>
 */
public final class GameServer {
    /**
     * The server port. Defaults to 4321.
     */
    private Integer port = 4321;
    /**
     * All the clients connected to this server instance
     */
    private final ChannelGroup managedClients;
    /**
     *
     */
    private PlayerServerFacade playerServerFacade;
    /**
     *
     */
    private LobbyServerFacade lobbyServerFacade;

    private GameServer() {
        managedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        playerServerFacade = new PlayerServerFacade();
        injectManagedClientsIntoServerFacade(playerServerFacade);
        lobbyServerFacade = new LobbyServerFacade(playerServerFacade);
        injectManagedClientsIntoServerFacade(lobbyServerFacade);
    }

    public static GameServer getInstance() {
        return new GameServer();
    }

    public GameServer withPort(final Integer port) {
        if (port <= 1024)
            throw new IllegalArgumentException("Port number must not belong to the interval [1, 1024], which is a reserved port pool, or be lesser than its lowest bound.");
        this.port = port;
        return this;
    }

    public GameServer withIDServerFacade(final PlayerServerFacade iDServerFacade) {
        this.playerServerFacade = iDServerFacade;
        injectManagedClientsIntoServerFacade(iDServerFacade);
        return this;
    }

    public GameServer withLobbyRequestServerFacade(final LobbyServerFacade lobbyServerFacade) {
        this.lobbyServerFacade = lobbyServerFacade;
        injectManagedClientsIntoServerFacade(lobbyServerFacade);
        return this;
    }

    public void run() throws Exception {
        final var bossGroup = new NioEventLoopGroup();
        final var workerGroup = new NioEventLoopGroup();
        try {
            final var bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new GameServerInitializer(
                            playerServerFacade,
                            lobbyServerFacade
                    ))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            final var future = bootstrap.bind(port).sync();
            System.out.printf("GameServer running on port %d\n", port);

            //Blocking method
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void injectManagedClientsIntoServerFacade(ServerFacade<? extends PDU> serverFacade) {
        Stream.of(serverFacade.getClass().getSuperclass().getDeclaredFields())
                .filter(field -> field.getType().equals(ChannelGroup.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(serverFacade, managedClients);
                    } catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }
}
