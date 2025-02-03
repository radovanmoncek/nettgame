package client.ship.parents.facades;

import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.ship.parents.pdus.PDU;

public abstract class ChannelPDUCommunicationsHandler<P extends PDU> extends SimpleChannelInboundHandler<P> {
    private static Channel serverChannel;

    protected final void unicastPDUToServerChannel(final PDU protocolDataUnit) {
        serverChannel.writeAndFlush(protocolDataUnit);
    }
}
