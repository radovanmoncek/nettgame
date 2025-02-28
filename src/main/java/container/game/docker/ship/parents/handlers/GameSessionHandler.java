package container.game.docker.ship.parents.handlers;

import container.game.docker.ship.enums.GameSessionConfigurationOption;
import container.game.docker.ship.parents.models.GameSessionContext;
import container.game.docker.ship.models.GameSessionConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class GameSessionHandler<FlatBuffersSchema> extends ChannelGroupHandler<FlatBuffersSchema> {
    private static final Logger logger;
    private static final ConcurrentHashMap<UUID, BlockingQueue<Channel>> playerConnectionEventQueueRegistry;
    private static final ChannelGroup connectedPlayers;

    static {

        logger = LogManager.getLogger(GameSessionHandler.class);
        playerConnectionEventQueueRegistry = new ConcurrentHashMap<>();
        connectedPlayers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    protected final void startGameSession(final GameSessionConfiguration gameSessionConfiguration) {

        if (!validateGameSessionConfiguration(gameSessionConfiguration)){

            logger.debug("Game session configuration is invalid");

            return;
        }

        final var gameSessionUpdateListener = gameSessionConfiguration.getGameSessionUpdateHandler();
        final var sessionRunnable = new Runnable() {
            private final ChannelGroup sessionMembers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            private final UUID sessionUUID = UUID.randomUUID();

            {

                playerConnectionEventQueueRegistry.putIfAbsent(sessionUUID, new LinkedBlockingQueue<>());
            }

            @Override
            public void run() {

                final var gameSessionContext = new GameSessionContext() {
                    private final ChannelGroup channelGroup = sessionMembers;

                    @Override
                    public void broadcast(Object message) {

                        channelGroup.writeAndFlush(message);
                    }

                    @Override
                    public Optional<UUID> retrieveGameSessionUUID() {

                        return Optional.of(sessionUUID);
                    }

                    @Override
                    public List<Channel> retrievePlayerChannels() {

                        return List.of(channelGroup.toArray(Channel[]::new));
                    }
                };

                try {

                    final var pendingChannel = playerConnectionEventQueueRegistry
                            .get(sessionUUID)
                            .take();

                    if(sessionMembers.size() > gameSessionConfiguration.getMaxPlayers()) {
                        return;
                    }

                    sessionMembers.add(pendingChannel);
                } catch (InterruptedException interruptedException) {

                    logger.error(interruptedException);
                }

                final var gameSessionUUIDOptional = gameSessionContext.retrieveGameSessionUUID();

                if (gameSessionUUIDOptional.isEmpty()) {

                    logger.error("Game session context is missing UUID, this should NEVER occur!");

                    return;
                }

                final var gameSessionPlayerChannels = gameSessionContext.retrievePlayerChannels();

                if(gameSessionPlayerChannels.isEmpty()) {

                    logger.error("Game session context is missing host playerChannel, this should NEVER occur!");

                    return;
                }

                gameSessionUpdateListener.onSessionStart(gameSessionContext);

                while (!sessionMembers.isEmpty() || gameSessionConfiguration.containsOption(GameSessionConfigurationOption.KEEP_ALIVE)) {

                    try {

                        sessionMembers.removeIf(sessionMember -> !connectedPlayers.contains(sessionMember));

                        gameSessionUpdateListener.onSessionRunning(gameSessionContext);

                        TimeUnit.MILLISECONDS.sleep(33);
                    } catch (final Exception exception) {

                        logger.error(exception.getMessage(), exception);

                        gameSessionUpdateListener.onSessionErrorThrown(gameSessionContext, exception);

                        break;
                    }
                }

                gameSessionUpdateListener.onSessionEnded(gameSessionContext);



                playerConnectionEventQueueRegistry.remove(sessionUUID);

                logger.info("Session {} has ended", sessionUUID);
            }
        };

        final var sessionThread = Executors
                .defaultThreadFactory()
                .newThread(sessionRunnable);

        sessionThread.setName(String.format("Game session %s", sessionRunnable.sessionUUID));
        sessionThread.start();

        logger.warn("Game Session {} Thread started", sessionRunnable.sessionUUID);
    }

    protected final void registerPlayerConnectionEvent(final UUID sessionUUID, final Channel playerChannel) {

        connectedPlayers.add(playerChannel);

        if(!playerConnectionEventQueueRegistry.containsKey(sessionUUID))
            return;

        final var playerConnectionEventEnqueueResult = playerConnectionEventQueueRegistry
                .get(sessionUUID)
                .offer(playerChannel);

        logger.warn("Player session connection event enqueue result {}", playerConnectionEventEnqueueResult);
    }

    protected final void deregisterPlayerFromSessionConnections(final Channel playerChannel) {

        connectedPlayers.remove(playerChannel);

        logger.warn("Player attempted deregister from game session");
    }

    private boolean validateGameSessionConfiguration(final GameSessionConfiguration gameSessionConfiguration) {

        int initialAssertionFlag = 0;

        if (!connectedPlayers.contains(gameSessionConfiguration.getHostChannel()))
            initialAssertionFlag += 1;

        if(gameSessionConfiguration.getGameSessionUpdateHandler() != null)
            initialAssertionFlag += 1;

        return initialAssertionFlag == 2;
    }
}
