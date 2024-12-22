package server.game.docker.ship.parents.facades;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.List;
import java.util.stream.Stream;

public abstract class ChannelGroupFacade<P extends PDU> {
    private ChannelGroup clientChannels;

    public final void startManagingClient(final Channel clientChannel){
        clientChannels.add(clientChannel);
    }

    public final void stopManagingClient(final Channel clientChannel){
        clientChannels.remove(clientChannel);
    }

    protected final void unicastPDUToClientChannel(final P protocolDataUnit, final Channel clientChannel){
        clientChannels.find(clientChannel.id()).writeAndFlush(protocolDataUnit);
    }

    protected final void multicastPDUToClientChannels(final P protocolDataUnit, final Channel ... clientChannels){
        List.of(clientChannels).forEach(channel -> unicastPDUToClientChannel(protocolDataUnit, channel));
    }

    protected final void multicastPDUToClientChannelIds(final P protocolDataUnit, final ChannelId... clientChannelIds){
        multicastPDUToClientChannels(protocolDataUnit, Stream.of(clientChannelIds).map(clientChannels::find).toArray(Channel[]::new));
    }
}
