package container.game.docker.ship.parents.handlers;

import container.game.docker.modules.session.handlers.SessionChannelGroupHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import container.game.docker.ship.parents.models.ProtocolDataUnit;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public abstract class ChannelGroupHandler<P1 extends ProtocolDataUnit, P2 extends ProtocolDataUnit> extends SimpleChannelInboundHandler<P1> {
    private static final ConcurrentMap<Integer, ConcurrentLinkedQueue<SessionMessage>> concurrentThreadMessageQueues = new ConcurrentHashMap<>();
    private ChannelGroup clientChannels;
    private ThreadGroup managedSessions;
    private int managedSessionsCount = 0;

    protected final void startManagingClient(final Channel clientChannel){
        clientChannels.add(clientChannel);
    }

    protected final void stopManagingClient(final Channel clientChannel){
        clientChannels.remove(clientChannel);
    }

    protected final void startManagingSession(final Runnable runnable){
        new Thread(managedSessions, runnable, String.format("Game Session Thread %d", ++managedSessionsCount)).start();
    }

    protected final void offerSession(final int sessionHash, final ProtocolDataUnit protocolDataUnit, final ChannelHandlerContext channelHandlerContext){
        concurrentThreadMessageQueues.get(sessionHash).offer(new SessionMessage(protocolDataUnit, channelHandlerContext));
    }

    protected final void unicastPDUToClientChannel(final P2 protocolDataUnit, final Channel clientChannel){
        clientChannels.find(clientChannel.id()).writeAndFlush(protocolDataUnit);
    }

    protected final void multicastPDUToClientChannels(final P2 protocolDataUnit, final Channel ... clientChannels){
        List.of(clientChannels).forEach(channel -> unicastPDUToClientChannel(protocolDataUnit, channel));
    }

    protected final void multicastPDUToClientChannelIds(final P2 protocolDataUnit, final ChannelId... clientChannelIds){
        multicastPDUToClientChannels(protocolDataUnit, Stream.of(clientChannelIds).map(clientChannels::find).toArray(Channel[]::new));
    }

    private record SessionMessage(ProtocolDataUnit protocolDataUnit, ChannelHandlerContext channelHandlerContext) {}
}
