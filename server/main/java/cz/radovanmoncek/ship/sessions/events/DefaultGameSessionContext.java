package cz.radovanmoncek.ship.sessions.events;

import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultGameSessionContext implements GameSessionContext {
    private static final Logger logger = Logger.getLogger(DefaultGameSessionContext.class.getName());
    private final ChannelGroup globalConnections;
    private final ChannelGroup contextConnections;
    private final LinkedBlockingQueue<Channel> pendingChannels;

    public DefaultGameSessionContext(ChannelGroup globalConnections, ChannelGroup contextConnections, LinkedBlockingQueue<Channel> pendingChannels) {

        this.globalConnections = globalConnections;
        this.contextConnections = contextConnections;
        this.pendingChannels = pendingChannels;
    }

    @Override
    public void broadcast(Object message) {

        contextConnections.writeAndFlush(message);
    }

    @Override
    public void performOnAllConnections(Consumer<Channel> action) {

        for (final var channel : contextConnections) {

            action.accept(channel);
        }
    }

    public final void registerPlayerConnection(final Channel playerChannel) {

        final var playerConnectionEventEnqueueResult = pendingChannels.offer(playerChannel);

        logger.log(Level.INFO, "Player session connection event enqueue result {0}", playerConnectionEventEnqueueResult);
    }

    public final void unregisterPlayerConnection(final Channel playerChannel) {

        globalConnections.remove(playerChannel);

        logger.warning("Player attempted deregister from game session");
    }

    @Override
    public void performOnAllConnectionsWithIndex(BiConsumer<Channel, Integer> action) {

        int count = 0;

        for (final var channel : contextConnections) {

            action.accept(channel, count++);
        }
    }
}
