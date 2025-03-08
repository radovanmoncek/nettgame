package cz.radovanmoncek.ship.parents.handlers;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.parents.sessions.listeners.GameSessionEventListener;
import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import cz.radovanmoncek.ship.sessions.events.GameSessionEventLoop;
import cz.radovanmoncek.ship.sessions.models.DefaultGameSessionContext;
import cz.radovanmoncek.ship.sessions.models.GameSessionConfigurationOption;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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

        logger = LogManager.getLogger(GameSessionChannelGroupHandler.class);
        globalConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        gameSessionListeners = new ConcurrentLinkedQueue<>();
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * Broadcasts a connection event to all registered {@link GameSessionEventListener}s.
     * This method is very computationally intensive; it should only be used for events that necessitate global event attention.
     *
     * @param playerChannel the channel that is relevant to the arisen event.
     */
    protected void broadcastEvent(final Channel playerChannel) {

        gameSessionListeners.forEach(entry -> entry.getKey().onGlobalConnectionEvent(entry.getValue(), playerChannel));
    }

    /**
     * Starts a new game session with the given listener and options.
     * Inspired by the Jetty Java server.
     */
    protected final void startGameSession(final GameSessionEventListener gameSessionEventListener, final List<Map.Entry<GameSessionConfigurationOption, Object>> gameSessionOptions) {

        if (!validate(gameSessionEventListener, gameSessionOptions)) {

            logger.warn("Game session configuration is invalid");

            return;
        }

        final var contextConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        final var pendingConnections = new LinkedBlockingQueue<Channel>();
        final var gameSessionContext = new DefaultGameSessionContext(globalConnections, contextConnections, pendingConnections);
        final var gameSessionListenerEntry = new AbstractMap.SimpleEntry<GameSessionEventListener, GameSessionContext>(gameSessionEventListener, gameSessionContext);
        final var gameSessionEventLoop = new GameSessionEventLoop(
                gameSessionContext,
                globalConnections,
                contextConnections,
                pendingConnections,
                gameSessionOptions,
                gameSessionListenerEntry,
                gameSessionListeners,
                gameSessionEventListener
        );

        gameSessionListeners.add(gameSessionListenerEntry);

        executorService.submit(gameSessionEventLoop);

        logger.info("New game session started");
    }

    private boolean validate(GameSessionEventListener gameSessionEventListener, List<Map.Entry<GameSessionConfigurationOption, Object>> options) {

        return options != null && gameSessionEventListener != null && options
                .stream()
                .anyMatch(entry -> entry.getKey().equals(GameSessionConfigurationOption.MAX_PLAYERS));
    }
}
