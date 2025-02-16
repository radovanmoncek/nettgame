package container.game.docker.ship.bootstrap;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.function.Supplier;

public final class InstanceContainerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LogManager.getLogger(InstanceContainerChannelInitializer.class);
    private final LinkedList<Supplier<? extends ChannelHandler>> channelGroupHandlerSuppliers;

    public InstanceContainerChannelInitializer(LinkedList<Supplier<? extends ChannelHandler>> channelHandlerSuppliers) {

        this.channelGroupHandlerSuppliers = channelHandlerSuppliers;
    }

    @Override
    public void initChannel(final SocketChannel socketChannel) {

        socketChannel.pipeline().addFirst(new LoggingHandler(LogLevel.INFO));

        channelGroupHandlerSuppliers.stream().map(Supplier::get).forEach(socketChannel.pipeline()::addLast);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
