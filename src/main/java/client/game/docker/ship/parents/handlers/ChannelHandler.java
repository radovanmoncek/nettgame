package client.game.docker.ship.parents.handlers;

import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ChannelHandler<P1 extends ProtocolDataUnit, P2 extends ProtocolDataUnit> extends SimpleChannelInboundHandler<P1> {
    private static final Logger logger = LogManager.getLogger(ChannelHandler.class);
    private Channel serverChannel;

    @Override
    protected final void channelRead0(final ChannelHandlerContext channelHandlerContext, final P1 protocolDataUnit) {

        serverChannelRead(protocolDataUnit);
    }

    @Override
    public final void handlerAdded(final ChannelHandlerContext channelHandlerContext) {

        serverChannel = channelHandlerContext.channel();
    }

    protected final void unicastToServerChannel(final P2 protocolDataUnit) {

        if(serverChannel == null){

            logger.error("Couldn't send message to server, channel is null");

            return;
        }

        serverChannel.writeAndFlush(protocolDataUnit);
    }

    abstract protected void serverChannelRead(final P1 protocolDataUnit);
}
