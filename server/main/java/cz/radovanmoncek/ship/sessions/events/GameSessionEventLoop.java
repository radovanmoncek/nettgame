package cz.radovanmoncek.ship.sessions.events;

import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import cz.radovanmoncek.ship.parents.sessions.listeners.GameSessionEventListener;
import cz.radovanmoncek.ship.sessions.models.GameSessionConfigurationOption;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GameSessionEventLoop implements Runnable {
    private static final Logger logger = LogManager.getLogger(GameSessionEventLoop.class);
    private final GameSessionContext gameSessionContext;
    private final ChannelGroup globalConnections;
    private final ChannelGroup contextConnections;
    private final LinkedBlockingQueue<Channel> pendingChannels;
    private final List<Map.Entry<GameSessionConfigurationOption, Object>> options;
    private final Map.Entry<GameSessionEventListener, GameSessionContext> gameSessionListenerEntry;
    private final GameSessionEventListener gameSessionEventListener;
    private final ConcurrentLinkedQueue<Map.Entry<GameSessionEventListener, GameSessionContext>> gameSessionListeners;
    private boolean endedCheck;

    public GameSessionEventLoop(
            GameSessionContext gameSessionContext,
            ChannelGroup globalConnections,
            ChannelGroup contextConnections,
            LinkedBlockingQueue<Channel> pendingChannels,
            List<Map.Entry<GameSessionConfigurationOption, Object>> options, Map.Entry<GameSessionEventListener, GameSessionContext> gameSessionListenerEntry,
            ConcurrentLinkedQueue<Map.Entry<GameSessionEventListener, GameSessionContext>> gameSessionListeners,
            GameSessionEventListener gameSessionEventListener
    ) {

        this.gameSessionContext = gameSessionContext;
        this.globalConnections = globalConnections;
        this.contextConnections = contextConnections;
        this.pendingChannels = pendingChannels;
        this.options = options;
        this.gameSessionListenerEntry = gameSessionListenerEntry;
        this.gameSessionListeners = gameSessionListeners;
        this.gameSessionEventListener = gameSessionEventListener;

        endedCheck = false;
    }

    @Override
    public void run() {

        try {

            gameSessionEventListener.onInitialize(gameSessionContext);

            var pendingChannel = pendingChannels.take(); //todo: option: wait for initial channel

            if (retrieveOption(GameSessionConfigurationOption.MAX_PLAYERS) instanceof Integer maxPlayers && contextConnections.size() > maxPlayers) {

                return;
            }

            contextConnections.add(pendingChannel);
            globalConnections.add(pendingChannel);

            gameSessionEventListener.onStart(gameSessionContext);

            boolean runCondition = !contextConnections.isEmpty();
            long timeoutMilliseconds;

            while (runCondition) {

                runCondition = !contextConnections.isEmpty();

                /*if(!runCondition && gameSessionConfiguration.containsOption(GameSessionConfigurationOption.ENABLE_TIMEOUT)) {

                    timeoutMilliseconds = System.currentTimeMillis() + 20000;
                }

                if(timeoutMilliseconds > System.currentTimeMillis())
                    runCondition = true;*/

                if (contextConnections.stream().noneMatch(globalConnections::contains)) {

                    gameSessionEventListener.onContextConnectionCountChanged(gameSessionContext);
                    gameSessionEventListener.onContextConnectionsEmpty(gameSessionContext);
                }

                contextConnections.removeIf(sessionMember -> {

                    if (!globalConnections.contains(sessionMember)) {

                        gameSessionEventListener.onContextConnectionCountChanged(gameSessionContext);
                        gameSessionEventListener.onContextConnectionClosed(gameSessionContext);

                        return true;
                    }

                    return false;
                });

                pendingChannel = pendingChannels.poll();

                if (Objects.nonNull(pendingChannel)) {

                    if (retrieveOption(GameSessionConfigurationOption.MAX_PLAYERS) instanceof Integer maxPlayers && contextConnections.size() < maxPlayers) {

                        if (contextConnections.add(pendingChannel) && globalConnections.add(pendingChannel)) {

                            gameSessionEventListener.onContextConnection(gameSessionContext, pendingChannel);
                            gameSessionEventListener.onContextConnectionCountChanged(gameSessionContext);
                        }
                    }
                }

                gameSessionEventListener.onRunning(gameSessionContext);

                TimeUnit.MILLISECONDS.sleep(33); //todo: better tick rate server mechanism
            }

            gameSessionListeners.remove(gameSessionListenerEntry); //todo: resolve externally in bootstrap

            logger.info("GameSessionEventLoop end {} {}", contextConnections, gameSessionListeners); //todo: debug

            if (endedCheck) {

                logger.error("GameSessionEventLoop stopped more than once, THIS SHOULD NEVER HAPPEN!");

                return;
            }

            gameSessionEventListener.onEnded(gameSessionContext);

            endedCheck = true;
        } catch (final Exception exception) {

            logger.error(exception.getMessage(), exception);

            gameSessionEventListener.onErrorThrown(gameSessionContext, exception);
        }
    }

    private Object retrieveOption(GameSessionConfigurationOption gameSessionConfigurationOption) {

        return options
                .stream()
                .filter(entry -> entry.getKey().equals(gameSessionConfigurationOption))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(null);
    }
}
