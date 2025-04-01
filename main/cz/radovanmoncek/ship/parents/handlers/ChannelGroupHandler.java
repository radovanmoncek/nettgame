package cz.radovanmoncek.ship.parents.handlers;

import com.google.flatbuffers.Table;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is mostly a utility handler for managing connected channels using a {@link ChannelGroup} object.
 * @param <FlatBuffersSchema> the FlatBuffers schema this handler should accept.
 */
public abstract class ChannelGroupHandler<FlatBuffersSchema extends Table> extends SimpleChannelInboundHandler<FlatBuffersSchema> {
    private static final Logger logger;
    /**
     * All the client channels connected to this server.
     */
    private static final ChannelGroup clientChannels;

    static {

        logger = Logger.getLogger(ChannelGroupHandler.class.getName());
        clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    /**
     * Starts managing the newly connected channel.
     * {@code super.handlerAdded(ChannelHandlerContext channelHandlerContext)} must be called to properly override this method.
     * @param channelHandlerContext refer to {@link SimpleChannelInboundHandler#handlerAdded(ChannelHandlerContext)}
     */
    @Override
    public void handlerAdded(final ChannelHandlerContext channelHandlerContext) {

        if(!clientChannels.add(channelHandlerContext.channel()))
            return;

        logger.log(Level.INFO, "Channel {0} registered", channelHandlerContext.channel());
    }

    protected void multicast(final Object message, final Channel ... clientChannels){

        for(final var channel : clientChannels){

            channel.writeAndFlush(message);
        }
    }

    protected void broadcast(final Object message){

        clientChannels.writeAndFlush(message);
    }
}
