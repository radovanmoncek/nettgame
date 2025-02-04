package client.ship.parents.handlers;

import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;

public abstract class ChannelHandler<P extends ProtocolDataUnit> extends SimpleChannelInboundHandler<P> {
    private static Channel serverChannel;

    protected final void unicastPDUToServerChannel(final ProtocolDataUnit protocolDataUnit) {
        serverChannel.writeAndFlush(protocolDataUnit);
    }
}
