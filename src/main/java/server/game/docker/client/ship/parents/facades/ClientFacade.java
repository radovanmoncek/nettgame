package server.game.docker.client.ship.parents.facades;

import io.netty.channel.Channel;
import server.game.docker.ship.parents.pdus.PDU;

public abstract class ClientFacade<P extends PDU> {
    private Channel serverChannel;

    protected final void unicastPDUToServerChannel(final P protocolDataUnit) {
        serverChannel.writeAndFlush(protocolDataUnit);
    }
}
