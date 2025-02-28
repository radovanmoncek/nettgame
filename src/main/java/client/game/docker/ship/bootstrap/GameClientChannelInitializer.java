package client.game.docker.ship.bootstrap;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public final class GameClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LogManager.getLogger(GameClientChannelInitializer.class);
    private final LinkedList<ChannelHandler> channelHandlers;

    public GameClientChannelInitializer(final LinkedList<ChannelHandler> channelHandlers) {

        this.channelHandlers = channelHandlers;
    }

    @Override
    protected void initChannel(final SocketChannel socketChannel) {

        channelHandlers.forEach(socketChannel.pipeline()::addLast);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
