package cz.radovanmoncek.nettgame.nettgame.ship.bay.events;

import cz.radovanmoncek.nettgame.nettgame.ship.deck.events.GameSessionEventListener;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.models.GameSessionConfigurationOption;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSessionEventLoop implements Runnable {

    private static final Logger logger = Logger.getLogger(GameSessionEventLoop.class.getName());

    private final GameSessionContext gameSessionContext;
    private final GameSessionEventListener gameSessionEventListener;
    private final List<Map.Entry<GameSessionConfigurationOption, Object>> options;
    private final ChannelGroup globalConnections;
    private final ChannelGroup contextConnections;
    private final LinkedBlockingQueue<Channel> pendingConnections;

    private boolean endedCheck;

    public GameSessionEventLoop(ChannelGroup globalConnections, GameSessionEventListener gameSessionEventListener, List<Map.Entry<GameSessionConfigurationOption, Object>> options) {

        this.globalConnections = globalConnections;
        this.gameSessionEventListener = gameSessionEventListener;
        this.options = options;

        contextConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        pendingConnections = new LinkedBlockingQueue<>();
        gameSessionContext = new GameSessionContext(globalConnections, contextConnections, pendingConnections);
        endedCheck = false;
    }

    public void notifyOfGlobalConnectionEvent(final Channel channel) {

        gameSessionEventListener.onGlobalConnectionEvent(gameSessionContext, channel);
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
                // we want each cycle to take, at most, 17 ms (60 Hz -> 60 cycles per second), and, the currentTickDelta be as close as possible to that value.
                // We sleep in the range [0, 17)
                TimeUnit.MILLISECONDS.sleep(Math.abs(currentTickDelta >= 17 ? 0 : sleepDelta));
            }

            logger.log(Level.FINEST, "GameSessionEventLoop end {0}", new Object[]{contextConnections});

            if (endedCheck) {

                logger.severe("GameSessionEventLoop stopped more than once, THIS SHOULD NEVER HAPPEN!");
                logger.warning("Terminating ...");

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
