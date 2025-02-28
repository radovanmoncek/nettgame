package container.game.docker.ship.parents.handlers;

import container.game.docker.ship.parents.products.Product;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class ChannelGroupHandler<FlatBuffersSchema> extends SimpleChannelInboundHandler<FlatBuffersSchema> implements Product {
    private static final Logger logger;
    /**
     * All the client channels connected to this server.
     */
    private static final ChannelGroup clientChannels;

    static {

        logger = LogManager.getLogger(ChannelGroupHandler.class);
        clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @Override
    protected final void channelRead0(final ChannelHandlerContext channelHandlerContext, final FlatBuffersSchema protocolDataUnit) {

        playerChannelRead(protocolDataUnit, channelHandlerContext.channel());
    }

    abstract protected void playerChannelRead(final FlatBuffersSchema flatBuffersSchema, final Channel playerChannel);

    @Override
    public final void handlerAdded(final ChannelHandlerContext channelHandlerContext) {

        if(!clientChannels.add(channelHandlerContext.channel()))
            return;

        logger.info("Player with ChannelId {} has connected", channelHandlerContext.channel().id());
    }

    @Override
    public final void channelUnregistered(final ChannelHandlerContext channelHandlerContext) {

        playerDisconnected(channelHandlerContext.channel());

        logger.info("Player with ChannelId {} has disconnected", channelHandlerContext.channel().id());
    }

    @Override
    public final void channelRegistered(final ChannelHandlerContext channelHandlerContext) {

        logger.info("container.game.docker.ship.parents.products.Product.Player Channel with ChannelId {} has registered", channelHandlerContext.channel().id());
    }

    abstract protected void playerDisconnected(final Channel playerChannel);

    protected final void multicast(final Object message, final Channel ... clientChannels){

        List
                .of(clientChannels)
                .forEach(channel -> channel.writeAndFlush(message));
    }

    protected final void broadcast(final Object message){

        clientChannels.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
