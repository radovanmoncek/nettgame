package client.game.docker.ship.parents.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;

public abstract class ChannelHandler<P1 extends ProtocolDataUnit, P2 extends ProtocolDataUnit> extends SimpleChannelInboundHandler<P1> {
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

            System.err.println("Couldnt send message to server, channel is null"); // todo: log4j

            return;
        }

        serverChannel.writeAndFlush(protocolDataUnit);
    }

    abstract protected void serverChannelRead(final P1 protocolDataUnit);
}
