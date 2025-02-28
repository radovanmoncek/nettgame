package client.game.docker.ship.parents.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ServerChannelHandler<FlatBuffersSchema> extends SimpleChannelInboundHandler<FlatBuffersSchema> {
    private static final Logger logger = LogManager.getLogger(ServerChannelHandler.class);
    private Channel serverChannel;

    @Override
    protected final void channelRead0(final ChannelHandlerContext channelHandlerContext, final FlatBuffersSchema flatBuffersSchema) {

        serverChannelRead(flatBuffersSchema);
    }

    @Override
    public final void handlerAdded(final ChannelHandlerContext channelHandlerContext) {

        serverChannel = channelHandlerContext.channel();
    }

    protected final void unicastToServerChannel(final Object message) {

        if(serverChannel == null){

            logger.error("Couldn't send message to server, channel is null");

            return;
        }

        serverChannel.writeAndFlush(message);
    }

    protected final boolean disconnectFromInstanceContainer() throws InterruptedException {

        serverChannel.close();

        final var afterChannelClosed = serverChannel.closeFuture().sync();

        return afterChannelClosed.isSuccess();
    }

    abstract protected void serverChannelRead(final FlatBuffersSchema flatBuffersSchema);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
