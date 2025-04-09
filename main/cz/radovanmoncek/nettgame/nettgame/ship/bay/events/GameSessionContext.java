package cz.radovanmoncek.nettgame.nettgame.ship.bay.events;

import cz.radovanmoncek.nettgame.nettgame.ship.deck.events.GameSessionEventListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inspired by the Netty {@link ChannelHandlerContext}, this class serves as an object
 * representing the current relevant game session state passed between individual state changing events.
 *
 * @apiNote This class contains several crucial utilities for manipulating connections.
 * @author Radovan Monček
 * @since 1.0
 * @see GameSessionEventListener
 */
public class GameSessionContext { //TODO: this class is a code smell, refactor

    private static final Logger logger = Logger.getLogger(GameSessionContext.class.getName());

    private final ChannelGroup globalConnections;
    private final ChannelGroup contextConnections;
    private final LinkedBlockingQueue<Channel> pendingChannels;

    /**
     * Constructs a new instance.
     * @param globalConnections {@link Channel}s that are currently connected to some game session.
     * @param contextConnections {@link Channel}s currently connected to this game session.
     * @param pendingChannels {@link Channel}s to be connected to this game session.
     *
     * @since 1.0
     * @author Radovan Monček
     */
    public GameSessionContext(final ChannelGroup globalConnections, final ChannelGroup contextConnections, final LinkedBlockingQueue<Channel> pendingChannels) {

        this.globalConnections = globalConnections;
        this.contextConnections = contextConnections;
        this.pendingChannels = pendingChannels;
    }

    /**
     * Broadcast a message to all currently registered connections.
     *
     * @param message the message Object to broadcast.
     * @apiNote This is a utility method aiming to replace the construct:
     * <code>
     * context.performOnAllConnections(playerChannel -> playerChannel.writeAndFlush(message));
     * </code>
     */
    public void broadcast(Object message) {

        contextConnections.writeAndFlush(message);
    }

    /**
     * Performs a {@link Consumer#accept(Object)} call for each currently registered connection.
     *
     * @param action the action to perform.
     */
    public void performOnAllConnections(Consumer<Channel> action) {

        for (final var channel : contextConnections) {

            action.accept(channel);
        }
    }

    /**
     * Register a channel to this game session.
     *
     * @param playerChannel the {@link Channel} to register.
     */
    public final void registerPlayerConnection(final Channel playerChannel) {

        final var playerConnectionEventEnqueueResult = pendingChannels.offer(playerChannel);

        logger.log(Level.INFO, "Player session connection event enqueue result {0}", playerConnectionEventEnqueueResult);
    }

    /**
     * Unregister a {@link Channel} from this game session.
     *
     * @param playerChannel the {@link Channel} to unregister.
     */
    public final void unregisterPlayerConnection(final Channel playerChannel) {

        globalConnections.remove(playerChannel); //TODO: should not be there

        logger.warning("Player attempted deregister from game session");
    }

    /**
     * Performs a {@link BiConsumer#accept(Object, Object)} call for each connected channel with information about the current iteration number.
     * Please note that, since the connections are backed by a {@link Set} structure, the index number is purely orientational,
     * and does not (and can not) represent connection ordering.
     *
     * @param action the action to perform.
     * @apiNote This is a utility alternative to {@link GameSessionContext#performOnAllConnections(Consumer)};
     * if only an iteration over all current connections is desired, these two methods may be used interchangeably.
     */
    public void performOnAllConnectionsWithIndex(BiConsumer<Channel, Integer> action) {

        int count = 0;

        for (final var channel : contextConnections) {

            action.accept(channel, count++);
        }
    }
}
