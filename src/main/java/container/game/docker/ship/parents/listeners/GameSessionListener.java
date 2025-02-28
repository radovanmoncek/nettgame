package container.game.docker.ship.parents.listeners;

import container.game.docker.ship.parents.models.GameSessionContext;

public abstract class GameSessionListener {

    public abstract void onSessionErrorThrown(GameSessionContext context, Throwable t);

    public abstract void onSessionStart(GameSessionContext context);

    public abstract void onSessionRunning(GameSessionContext context);

    public abstract void onSessionEnded(GameSessionContext context);
}
