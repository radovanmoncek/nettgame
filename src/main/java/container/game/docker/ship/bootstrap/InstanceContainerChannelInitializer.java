package container.game.docker.ship.bootstrap;

import container.game.docker.ship.parents.creators.Creator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public final class InstanceContainerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LogManager.getLogger(InstanceContainerChannelInitializer.class);
    private final LinkedList<Creator> channelGroupHandlerSuppliers;

    public InstanceContainerChannelInitializer(LinkedList<Creator> channelHandlerSuppliers) {

        this.channelGroupHandlerSuppliers = channelHandlerSuppliers;
    }

    @Override
    public void initChannel(final SocketChannel socketChannel) {

        socketChannel
                .pipeline()
                .addFirst(new LoggingHandler(LogLevel.INFO));

        channelGroupHandlerSuppliers
                .stream()
                .map(creator -> (ChannelHandler) creator.newProduct())
                .forEach(socketChannel.pipeline()::addLast);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
