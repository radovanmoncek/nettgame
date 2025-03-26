package cz.radovanmoncek.ship.parents.handlers;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.parents.sessions.listeners.GameSessionEventListener;
import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import cz.radovanmoncek.ship.sessions.events.GameSessionEventLoop;
import cz.radovanmoncek.ship.sessions.models.GameSessionConfigurationOption;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This handler is responsible for provisioning and managing game sessions.
 *
 * @param <FlatBuffersSchema> the FlatBuffers schema this handler should accept.
 */
public abstract class GameSessionChannelGroupHandler<FlatBuffersSchema extends Table> extends ChannelGroupHandler<FlatBuffersSchema> {
    private static final Logger logger;
    private static final ChannelGroup globalConnections;
    private static final ConcurrentLinkedQueue<Map.Entry<GameSessionEventListener, GameSessionContext>> gameSessionListeners;
    private static final ExecutorService executorService;

    static {

        logger = Logger.getLogger(GameSessionChannelGroupHandler.class.getName());
        globalConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        gameSessionListeners = new ConcurrentLinkedQueue<>();
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * Broadcasts a connection event to all registered {@link GameSessionEventListener}s.
     * This method should only be used for events of global scope.
     *
     * @param playerChannel the channel that is relevant to the arisen event.
     */
    protected void broadcastGlobalEvent(final Channel playerChannel) {

        for(final var entry : gameSessionListeners)
            entry
                    .getKey()
                    .onGlobalConnectionEvent(entry.getValue(), playerChannel);
    }

    /**
     * Starts a new game session with the given listener and options.
     * Inspired by the Jetty Java server.
     */
    protected final void startGameSession(final GameSessionEventListener gameSessionEventListener, final List<Map.Entry<GameSessionConfigurationOption, Object>> gameSessionOptions) {

        if (!validate(gameSessionEventListener, gameSessionOptions)) {

            logger.warning("Game session configuration is invalid");

            return;
        }

        final var gameSessionEventLoop = new GameSessionEventLoop()
                .withOptions(gameSessionOptions)
                .withGlobalConnections(globalConnections)
                .withGameSessionListeners(gameSessionListeners)
                .withGameSessionEventListener(gameSessionEventListener);

        executorService.submit(gameSessionEventLoop);

        logger.info("New game session started");
    }

    private boolean validate(GameSessionEventListener gameSessionEventListener, List<Map.Entry<GameSessionConfigurationOption, Object>> options) {

        return options != null && gameSessionEventListener != null && options
                .stream()
                .anyMatch(entry -> entry.getKey().equals(GameSessionConfigurationOption.MAX_PLAYERS));
    }
}
