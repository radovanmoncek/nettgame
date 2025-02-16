package container.game.docker.ship.bootstrap;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.LinkedList;
import java.util.function.Supplier;

public final class InstanceContainerChannelInitializer extends ChannelInitializer<SocketChannel> {
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

        cause.printStackTrace(); //todo: log4j
    }
}
