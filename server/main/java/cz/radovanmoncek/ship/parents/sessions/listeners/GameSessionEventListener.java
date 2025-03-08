package cz.radovanmoncek.ship.parents.sessions.listeners;

import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import io.netty.channel.Channel;

public abstract class GameSessionEventListener {

    public abstract void onErrorThrown(GameSessionContext context, Throwable t);

    /**
     * The first event called in the game session lifecycle.
     * Initialization operations should be performed as of receiving this event.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onInitialize(GameSessionContext context);

    public abstract void onStart(GameSessionContext context);

    public abstract void onRunning(GameSessionContext context);

    /**
     * This is the last event called in the lifecycle of a game session.
     * Clean-up operations should be performed as a result of this event.
     * @param context this {@link GameSessionContext}.
     */
    public abstract void onEnded(GameSessionContext context);

    public abstract void onContextConnectionsEmpty(GameSessionContext context);

    public abstract void onContextConnection(GameSessionContext context, Channel channel);

    public abstract void onContextConnectionClosed(GameSessionContext context);

    public abstract void onContextConnectionCountChanged(GameSessionContext context);

    /**
     * An event that occurred within a {@link cz.radovanmoncek.ship.parents.handlers.GameSessionChannelGroupHandler}.
     * This event is important, and should require special attention.
     * @param context this {@link GameSessionContext}
     * @param playerChannel the relevant channel.
     */
    public abstract void onGlobalConnectionEvent(GameSessionContext context, Channel playerChannel);
}
