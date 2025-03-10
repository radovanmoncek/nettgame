package cz.radovanmoncek.client.ship.parents.handlers;

import com.google.flatbuffers.Table;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ServerChannelHandler<FlatBuffersSchema extends Table> extends SimpleChannelInboundHandler<FlatBuffersSchema> {
    private static final Logger logger = LogManager.getLogger(ServerChannelHandler.class);
    private Channel serverChannel;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {

        serverChannel = ctx.channel();
    }

    protected void unicast(final Object message) {

        if(serverChannel == null){

            logger.error("serverChannel is null");

            return;
        }

        serverChannel.writeAndFlush(message);
    }

    protected void disconnect() {

        serverChannel
                .close()
                .addListener(future -> logger.info("Disconnected with success status {}", future.isSuccess()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
