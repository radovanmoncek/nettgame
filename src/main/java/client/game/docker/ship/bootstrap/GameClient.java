package client.game.docker.ship.bootstrap;

import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * <p>
 *     The Docker Game Client object enabling networked socket communication with the Docker Game Server.
 * </p>
 * <p>
 *     This class uses the <a href=https://refactoring.guru>Singleton and Builder Design Patterns</a>, since it is bound to a Socket.
 *     There can exist at most one instance of this class per runtime.
 * </p>
 */
public final class GameClient {
    /**
     * Singleton instance
     */
    private static GameClient INSTANCE;
    private final LinkedList<Supplier<? extends io.netty.channel.ChannelHandler>> channelHandlerSuppliers;
    private final EventLoopGroup workerGroup;
    private InetAddress gameServerAddress;
    private int gameServerPort;
    private Channel serverChannel;

    private GameClient() {
        workerGroup = new NioEventLoopGroup();
        channelHandlerSuppliers = new LinkedList<>();
    }

    /**
     * @param reconnectIntervalSeconds the number of seconds between reconnect attempts.
     * @param failReconnectAfterAttempts the number of reconnect retries.
     */
    public void run(int reconnectIntervalSeconds, int failReconnectAfterAttempts) {

        try {

            final var bootstrap = new Bootstrap();

            bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new GameClientChannelInitializer(channelHandlerSuppliers));

            for (int i = 0; i < failReconnectAfterAttempts && serverChannel == null; i++) {

                try {

                    serverChannel = bootstrap
                            .connect(gameServerAddress, gameServerPort)
                            .sync()
                            .channel();

                    TimeUnit.SECONDS.sleep(reconnectIntervalSeconds);
                }
                catch (final Exception ignored) {}
            }
        }
        catch (final Exception exception) {

            exception.printStackTrace(); //todo: log4j

            workerGroup.shutdownGracefully();
        }
    }

    public static GameClient newInstance() throws Exception {

        if (Objects.isNull(INSTANCE))
            INSTANCE = new GameClient()
                    .withServerPort(4321)
                    .withGameServerAddress(InetAddress.getLocalHost());

        return INSTANCE;
    }

    public GameClient withServerPort(final int serverPort) {

        this.gameServerPort = serverPort;

        return this;
    }

    public GameClient withGameServerAddress(final InetAddress gameServerAddress) {

        this.gameServerAddress = gameServerAddress;

        return this;
    }

    public GameClient withChannelHandler(final Supplier<ChannelHandler<? extends ProtocolDataUnit, ? extends ProtocolDataUnit>> channelHandlerSupplier) {

        channelHandlerSuppliers.add(channelHandlerSupplier);

        return this;
    }

    public GameClient withEncoder(final Supplier<Encoder<? extends ProtocolDataUnit>> encoderSupplier) {

        channelHandlerSuppliers.add(encoderSupplier);

        return this;
    }

    public GameClient withDecoder(final Supplier<Decoder<? extends ProtocolDataUnit>> decoderSupplier) {

        channelHandlerSuppliers.add(decoderSupplier);

        return this;
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

    public void run() {
        run(0, 10);
    }
}
