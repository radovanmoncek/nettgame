package server.game.docker.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import server.game.docker.client.modules.requests.facades.LobbyRequestClientFacade;
import server.game.docker.client.modules.usernames.facades.UsernameClientFacade;
import server.game.docker.client.ship.parents.ClientFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class GameClient {
    private final EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private InetAddress gameServerAddress;
    private int gameServerPort;
    private Channel serverChannel;
    private UsernameClientFacade usernameClientFacade;
    private LobbyRequestClientFacade lobbyRequestClientFacade;

    private GameClient() throws Exception {
        usernameClientFacade = new UsernameClientFacade();
        lobbyRequestClientFacade = new LobbyRequestClientFacade();
        gameServerAddress = InetAddress.getByName("127.0.0.1");
        gameServerPort = 4321;
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
    }

    /**
     *
     * @param reconnectIntervalSeconds
     */
    public void run(int reconnectIntervalSeconds){
        try {
            bootstrap = bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ClientInitializer(
                            usernameClientFacade,
                            lobbyRequestClientFacade
                    ));
        }
        catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
        while (serverChannel == null) {
            try {
                serverChannel = bootstrap.connect(gameServerAddress, gameServerPort).sync().channel();
                TimeUnit.SECONDS.sleep(reconnectIntervalSeconds);
            } catch (Exception ignored) {
            }
        }
        injectServerChannelIntoClientFacade(usernameClientFacade);
        injectServerChannelIntoClientFacade(lobbyRequestClientFacade);
    }

    //todo: singleton !!!!
    public static GameClient newInstance() throws Exception {
        return new GameClient();
    }

    public GameClient withUsernameFacade(final UsernameClientFacade usernameClientFacade) {
        if (this.usernameClientFacade == null)
            throw new NullPointerException("usernameClientFacade is null");

        this.usernameClientFacade = usernameClientFacade;
        injectServerChannelIntoClientFacade(usernameClientFacade);
        return this;
    }

    public GameClient withLobbyReqFacade(final LobbyRequestClientFacade lobbyRequestClientFacade) {
        if(lobbyRequestClientFacade == null)
            throw new NullPointerException("lobbyRequestClientFacade cannot be null");

        this.lobbyRequestClientFacade = lobbyRequestClientFacade;
        injectServerChannelIntoClientFacade(lobbyRequestClientFacade);
        return this;
    }

    public GameClient withServerAddress(final InetAddress gameServerAddress) {
        this.gameServerAddress = gameServerAddress;
        return this;
    }

    public GameClient withServerPort(final int gameServerPort) {
        this.gameServerPort = gameServerPort;
        return this;
    }

    public InetAddress getServerAddress() {
        return gameServerAddress;
    }

    public void setServerChannel(Channel serverChannel) {
        this.serverChannel = serverChannel;
        injectServerChannelIntoClientFacade(usernameClientFacade);
        injectServerChannelIntoClientFacade(lobbyRequestClientFacade);
    }

    public UsernameClientFacade getUsernameClientFacade() {
        return usernameClientFacade;
    }

    public LobbyRequestClientFacade getLobbyReqFacade() {
        return lobbyRequestClientFacade;
    }

    public int getGameServerPort() {
        return gameServerPort;
    }

    public void shutdownGracefullyAfterNSeconds(final int seconds) throws InterruptedException {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                serverChannel.close();
            }
        }, (long) seconds * 1000);
        serverChannel.closeFuture().sync();
        workerGroup.shutdownGracefully();
    }

    public boolean isConnected() {
        return serverChannel != null && serverChannel.isActive();
    }

    private void injectServerChannelIntoClientFacade(final ClientFacade<? extends PDU> clientFacade) {
        var clazz = clientFacade.getClass().getSuperclass();
        while(Stream.of(clazz.getDeclaredFields()).map(Field::getType).noneMatch(Channel.class::equals)){
            clazz = clazz.getSuperclass();
        }
        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getType().equals(Channel.class))
                .findAny().ifPresent(field -> {
            field.setAccessible(true);
            try {
                field.set(clientFacade, serverChannel);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }
}
