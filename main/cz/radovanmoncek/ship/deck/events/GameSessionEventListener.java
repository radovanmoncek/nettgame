package cz.radovanmoncek.ship.deck.events;

import cz.radovanmoncek.ship.bay.parents.handlers.GameSessionChannelGroupHandler;
import cz.radovanmoncek.ship.bay.events.GameSessionEventLoop;
import io.netty.channel.Channel;

/**
 * Listens for {@link GameSessionEventLoop} events.
 * At most one {@link GameSessionEventListener} may be registered to each {@link GameSessionEventLoop}.
 * @see GameSessionEventLoop
 * @author Radovan Monček
 * @since 1.0
 */
public interface GameSessionEventListener {

    /**
     * If this event is called, some exception has occurred inside the {@link GameSessionEventLoop} and it had to unexpectedly end.
     * @param context this {@link GameSessionContext}.
     * @param t the exception that caused this event.
     */
    void onErrorThrown(GameSessionContext context, Throwable t);

    /**
     * The first event called in the game session lifecycle.
     * Initialization operations should be performed as of receiving this event.
     * @param context this {@link GameSessionContext}.
     */
    void onInitialize(GameSessionContext context);

    /**
     * This event is called once all preparation operations are complete and the game session has started.
     * @param context this {@link GameSessionContext}.
     */
    void onStart(GameSessionContext context);

    /**
     * This event is called for each {@link GameSessionEventLoop} cycle.
     * State change operations should be performed.
     * @param context this {@link GameSessionContext}.
     */
    void onServerTick(GameSessionContext context);

    /**
     * This is the last event called in the lifecycle of a game session.
     * Finalizing, and clean-up operations should be performed as a result of this event.
     * @param context this {@link GameSessionContext}.
     */
    void onEnded(GameSessionContext context);

    /**
     * This event is called if no connections currently exist.
     * @param context this {@link GameSessionContext}.
     */
    void onContextConnectionsEmpty(GameSessionContext context);

    /**
     * Called, when a {@link Channel} connects.
     * @param context this {@link GameSessionContext}.
     * @param channel the relevant {@link Channel}.
     */
    void onContextConnection(GameSessionContext context, Channel channel);

    /**
     * Called, when a {@link Channel} disconnects.
     * @param context this {@link GameSessionContext}.
     */
    void onContextConnectionClosed(GameSessionContext context);

    /**
     * This event is called if the internal {@link io.netty.channel.group.ChannelGroup} changes.
     * @param context this {@link GameSessionContext}.
     */
    void onContextConnectionCountChanged(GameSessionContext context);

    /**
     * An event that occurred within a {@link GameSessionChannelGroupHandler}.
     * This event is important, and should require special attention.
     * @param context this {@link GameSessionContext}
     * @param playerChannel the relevant channel.
     */
    void onGlobalConnectionEvent(GameSessionContext context, Channel playerChannel);
}
