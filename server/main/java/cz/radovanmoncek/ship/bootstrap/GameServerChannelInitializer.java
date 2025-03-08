package cz.radovanmoncek.ship.bootstrap;

import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public final class GameServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LogManager.getLogger(GameServerChannelInitializer.class);
    private final LinkedList<ChannelHandlerCreator> channelGroupHandlerSuppliers;
    private final LinkedList<ChannelHandler> initialHandlers;

    public GameServerChannelInitializer(final LinkedList<ChannelHandler> initialHandlers, final LinkedList<ChannelHandlerCreator> channelHandlerSuppliers) {

        this.initialHandlers = initialHandlers;
        this.channelGroupHandlerSuppliers = channelHandlerSuppliers;
    }

    @Override
    public void initChannel(final SocketChannel socketChannel) {

        initialHandlers.forEach(socketChannel.pipeline()::addLast);

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
