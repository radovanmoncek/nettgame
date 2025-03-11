package cz.radovanmoncek.ship.sessions.models;

import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class DefaultGameSessionContext implements GameSessionContext {
    private static final Logger logger = LogManager.getLogger(DefaultGameSessionContext.class);
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
    public void performOnAllConnections(Consumer<Channel> playerChannel) {

        for (final var channel : contextConnections) {

            playerChannel.accept(channel);
        }
    }

    public final void registerPlayerConnection(final Channel playerChannel) {

        final var playerConnectionEventEnqueueResult = pendingChannels.offer(playerChannel);

        logger.warn("Player session connection event enqueue result {}", playerConnectionEventEnqueueResult);
    }

    public final void unregisterPlayerConnection(final Channel playerChannel) {

        globalConnections.remove(playerChannel);

        logger.warn("Player attempted deregister from game session");
    }
}
