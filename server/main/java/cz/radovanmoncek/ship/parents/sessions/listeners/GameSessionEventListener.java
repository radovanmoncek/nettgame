package cz.radovanmoncek.ship.parents.sessions.listeners;

import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import io.netty.channel.Channel;

/**
 * Listens for {@link cz.radovanmoncek.ship.sessions.events.GameSessionEventLoop} events.
 * At most one {@link GameSessionEventListener} may be registered to each {@link cz.radovanmoncek.ship.sessions.events.GameSessionEventLoop}.
 * @see cz.radovanmoncek.ship.sessions.events.GameSessionEventLoop
 * @author Radovan Monček
 * @since 1.0
 */
public abstract class GameSessionEventListener {

    /**
     * If this event is called, some exception has occurred inside the {@link cz.radovanmoncek.ship.sessions.events.GameSessionEventLoop} and it had to unexpectedly end.
     * @param context this {@link GameSessionContext}.
     * @param t the exception that caused this event.
     */
    public abstract void onErrorThrown(GameSessionContext context, Throwable t);

    /**
     * The first event called in the game session lifecycle.
     * Initialization operations should be performed as of receiving this event.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onInitialize(GameSessionContext context);

    /**
     * This event is called once all preparation operations are complete and the game session has started.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onStart(GameSessionContext context);

    /**
     * This event is called for each {@link cz.radovanmoncek.ship.sessions.events.GameSessionEventLoop} cycle.
     * State change operations should be performed.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onServerTick(GameSessionContext context);

    /**
     * This is the last event called in the lifecycle of a game session.
     * Finalizing, and clean-up operations should be performed as a result of this event.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onEnded(GameSessionContext context);

    /**
     * This event is called if no connections currently exist.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onContextConnectionsEmpty(GameSessionContext context);

    /**
     * Called, when a {@link Channel} connects.
     * @param context this {@link GameSessionContext}.
     * @param channel the relevant {@link Channel}.
     */
    public abstract void onContextConnection(GameSessionContext context, Channel channel);

    /**
     * Called, when a {@link Channel} disconnects.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onContextConnectionClosed(GameSessionContext context);

    /**
     * This event is called if the internal {@link io.netty.channel.group.ChannelGroup} changes.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onContextConnectionCountChanged(GameSessionContext context);

    /**
     * An event that occurred within a {@link cz.radovanmoncek.ship.parents.handlers.GameSessionChannelGroupHandler}.
     * This event is important, and should require special attention.
     * @param context this {@link GameSessionContext}
     * @param playerChannel the relevant channel.
     */
    public abstract void onGlobalConnectionEvent(GameSessionContext context, Channel playerChannel);
}
