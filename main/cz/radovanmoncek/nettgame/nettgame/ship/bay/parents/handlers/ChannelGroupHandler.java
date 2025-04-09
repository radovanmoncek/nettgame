package cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.handlers;

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

    /**
     * Logger instance.
     */
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

    /**
     * Multicast a message to a group of selected clients, if their {@link Channel}s are registered by this handler.
     * @param message the message to send.
     * @param clientChannels the selected clients.
     */
    protected void multicast(final Object message, final Channel ... clientChannels){

        for(final var channel : clientChannels){

            if (!ChannelGroupHandler.clientChannels.contains(channel))
                return;

            channel.writeAndFlush(message);
        }
    }

    /**
     * Broadcast a message to all clients registered by this handler.
     * @param message the message to broadcast.
     */
    protected void broadcast(final Object message){

        clientChannels.writeAndFlush(message);
    }
}
