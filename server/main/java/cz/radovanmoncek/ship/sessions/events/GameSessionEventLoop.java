package cz.radovanmoncek.ship.sessions.events;

import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import cz.radovanmoncek.ship.parents.sessions.listeners.GameSessionEventListener;
import cz.radovanmoncek.ship.sessions.models.GameSessionConfigurationOption;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSessionEventLoop implements Runnable {
    private static final Logger logger = Logger.getLogger(GameSessionEventLoop.class.getName());
    private GameSessionContext gameSessionContext;
    private ChannelGroup globalConnections;
    private ChannelGroup contextConnections;
    private LinkedBlockingQueue<Channel> pendingConnections;
    private List<Map.Entry<GameSessionConfigurationOption, Object>> options;
    private Map.Entry<GameSessionEventListener, GameSessionContext> gameSessionListenerEntry;
    private GameSessionEventListener gameSessionEventListener;
    private ConcurrentLinkedQueue<Map.Entry<GameSessionEventListener, GameSessionContext>> gameSessionListeners;
    private boolean endedCheck;

    {

        endedCheck = false;
    }

    public GameSessionEventLoop withGlobalConnections(ChannelGroup globalConnections) {

        this.globalConnections = globalConnections;

        contextConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        pendingConnections = new LinkedBlockingQueue<>();
        gameSessionContext = new DefaultGameSessionContext(globalConnections, contextConnections, pendingConnections);

        return this;
    }

    public GameSessionEventLoop withOptions(List<Map.Entry<GameSessionConfigurationOption, Object>> options) {

        this.options = options;

        return this;
    }

    public GameSessionEventLoop withGameSessionEventListener(GameSessionEventListener gameSessionEventListener) {

        this.gameSessionEventListener = gameSessionEventListener;

        gameSessionListenerEntry = new AbstractMap.SimpleEntry<>(gameSessionEventListener, gameSessionContext);
        gameSessionListeners.add(gameSessionListenerEntry);

        return this;
    }

    public GameSessionEventLoop withGameSessionListeners(ConcurrentLinkedQueue<Map.Entry<GameSessionEventListener, GameSessionContext>> gameSessionListeners) {

        this.gameSessionListeners = gameSessionListeners;

        return this;
    }

    @Override
    public void run() {

        try {

            gameSessionEventListener.onInitialize(gameSessionContext);

            var pendingChannel = pendingConnections.take(); //todo: option: wait for initial channel

            if (retrieveOption(GameSessionConfigurationOption.MAX_PLAYERS) instanceof Integer maxPlayers && contextConnections.size() > maxPlayers) {

                return;
            }

            contextConnections.add(pendingChannel);
            globalConnections.add(pendingChannel);

            gameSessionEventListener.onStart(gameSessionContext);

            boolean runCondition = !contextConnections.isEmpty();
            long timeout = 0;
            boolean timeoutSet = false;

            // source and big thanks to: https://github.com/mas-bandwidth/yojimbo/blob/main/USAGE.md
            // https://en.wikipedia.org/wiki/Hertz -> we want 60 cycles per 1 second = 60 Hertz = 60^-1 = 1/60
            // the result is then converted to milliseconds -> approx. 16,7 ms -> 17 ms after rounding
            final var tickDelta = Math.round(1 / (float) 60 * 1000);

            while (runCondition || timeoutSet) {

                final var startingTime = System.currentTimeMillis();

                runCondition = !contextConnections.isEmpty();

                if (retrieveOption(GameSessionConfigurationOption.ENABLE_TIMEOUT) instanceof Boolean enableTimeout && enableTimeout) {

                    if(!runCondition) {

                        if (!timeoutSet) {

                            timeout = System.currentTimeMillis() + 20000;
                            timeoutSet = true;
                        }

                        if (timeout > System.currentTimeMillis())
                            break;
                    }
                    else
                        timeoutSet = false;
                }

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

                pendingChannel = pendingConnections.poll();

                if (Objects.nonNull(pendingChannel)) {

                    if (retrieveOption(GameSessionConfigurationOption.MAX_PLAYERS) instanceof Integer maxPlayers && contextConnections.size() < maxPlayers) {

                        if (contextConnections.add(pendingChannel) && globalConnections.add(pendingChannel)) {

                            gameSessionEventListener.onContextConnection(gameSessionContext, pendingChannel);
                            gameSessionEventListener.onContextConnectionCountChanged(gameSessionContext);
                        }
                    }
                }

                gameSessionEventListener.onServerTick(gameSessionContext);

                final var currentTickDelta = System.currentTimeMillis() - startingTime;
                final var sleepDelta = currentTickDelta - tickDelta;

                // https://github.com/mas-bandwidth/yojimbo/blob/main/USAGE.md
                // we want each cycle to take, at most, 17 ms, and, the currentTickDelta be as close as possible to that value.
                TimeUnit.MILLISECONDS.sleep(Math.abs(sleepDelta > 17 ? 0 : sleepDelta));
            }

            gameSessionListeners.remove(gameSessionListenerEntry); //todo: resolve externally in bootstrap

            logger.log(Level.FINEST, "GameSessionEventLoop end {0} {1}", new Object[]{contextConnections, gameSessionListeners});

            if (endedCheck) {

                logger.severe("GameSessionEventLoop stopped more than once, THIS SHOULD NEVER HAPPEN!");

                return;
            }

            gameSessionEventListener.onEnded(gameSessionContext);

            endedCheck = true;
        } catch (final Exception exception) {

            logger.throwing(getClass().getName(), "run", exception);

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
