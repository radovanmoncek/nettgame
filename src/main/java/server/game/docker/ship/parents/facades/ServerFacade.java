package server.game.docker.ship.parents.facades;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.List;

public abstract class ServerFacade<P extends PDU> {
    private ChannelGroup clientChannels;

    protected final void unicastPDUToClientChannel(final P protocolDataUnit, final Channel clientChannel){
        clientChannels.find(clientChannel.id()).writeAndFlush(protocolDataUnit);
    }

    protected final void broadcastPDUToChannelGroupExcept(final P protocolDataUnit, final Channel clientChannel){
        clientChannels.stream().filter(channel -> !channel.equals(clientChannel)).forEach(channel -> channel.writeAndFlush(protocolDataUnit));
    }

    protected final void multicastPDUToChannelGroup(final P protocolDataUnit, final ChannelGroup clientChannels){
        clientChannels.writeAndFlush(protocolDataUnit);
    }

    protected final void multicastPDUToClientChannels(final P protocolDataUnit, final Channel ... clientChannels){
        final var channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        channelGroup.addAll(List.of(clientChannels));
        multicastPDUToChannelGroup(protocolDataUnit, channelGroup);
    }

    public final void startManagingClient(final Channel clientChannel){
        clientChannels.add(clientChannel);
    }

    public final void stopManagingClient(final Channel clientChannel){
        clientChannels.remove(clientChannel);
    }
}
