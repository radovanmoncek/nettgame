package cz.radovanmoncek.modules.games.events;

import cz.radovanmoncek.modules.games.models.GameHistoryEntity;
import cz.radovanmoncek.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.modules.games.repositories.GameHistories;
import cz.radovanmoncek.ship.tables.Character;
import cz.radovanmoncek.ship.tables.GameStatus;
import cz.radovanmoncek.ship.tables.Request;
import cz.radovanmoncek.ship.parents.events.GameSessionEventListener;
import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import cz.radovanmoncek.ship.utilities.logging.LoggingUtilities;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExampleGameSessionEventListener implements GameSessionEventListener {
    private static final Logger logger = Logger.getLogger(ExampleGameSessionEventListener.class.getName());
    private static final AttributeKey<ConcurrentLinkedQueue<Request>> gameStateRequestQueueAttribute = AttributeKey.valueOf("gameStateRequestQueue");
    private static final AttributeKey<int[]> playerPositionAttribute = AttributeKey.valueOf("playerPosition");
    private static final AttributeKey<String> playerNameAttribute = AttributeKey.valueOf("playerName");
    private static final AttributeKey<Byte> playerCharacterAttribute = AttributeKey.valueOf("playerCharacter");
    private static final int xBound = 800,
                             yBound = 600,
                             moveDelta = 2;

    private final UUID gameUUID;
    private final Channel hostChannel;
    private final GameHistories gameHistories;
    private final String[] playerNames;
    private final Channel[] orderedPlayerChannels;
    private final long start;

    private int lastGameHash = Integer.MIN_VALUE;
    private long length;

    //https://stackoverflow.com/questions/6307648/change-global-setting-for-logger-instances
    static {

        LoggingUtilities.enableGlobalLoggingLevel(Level.ALL);
    }

    public ExampleGameSessionEventListener(Channel hostChannel, GameHistories gameHistories) {

        this.gameHistories = gameHistories;
        this.hostChannel = hostChannel;

        gameUUID = UUID.randomUUID();
        playerNames = new String[2];
        orderedPlayerChannels = new Channel[2];
        length = 0;
        start = System.currentTimeMillis();
    }

    @Override
    public void onErrorThrown(final GameSessionContext context, Throwable throwable) {

        logger.throwing(getClass().getName(), "onErrorThrown", throwable);
    }

    @Override
    public void onStart(final GameSessionContext context) {

        context.performOnAllConnections(playerChannel -> {

            final var playerGameStateRequestQueue = playerChannel
                    .attr(gameStateRequestQueueAttribute)
                    .get();

            if (playerGameStateRequestQueue == null)
                return;

            final var gameStateRequest = playerGameStateRequestQueue.poll();

            if (gameStateRequest == null)
                return;

            playerChannel
                    .attr(playerPositionAttribute)
                    .set(new int[]{xBound / 2, yBound / 2, 0});

            lastGameHash = Integer.hashCode(Arrays.stream(playerChannel.attr(playerPositionAttribute).get()).sum());

            playerChannel
                    .attr(playerNameAttribute)
                    .set(gameStateRequest.player().name());

            playerChannel
                    .attr(playerCharacterAttribute)
                    .set(gameStateRequest.player().character());

            playerNames[0] = playerChannel.attr(playerNameAttribute).get();

            playerChannel.writeAndFlush(new GameStateFlatBuffersSerializable()
                    .withGameState(length, GameStatus.START_SESSION, gameUUID.toString().substring(0, 8))
                    .withPlayer(playerChannel.attr(playerPositionAttribute).get(), playerChannel.attr(playerNameAttribute).get(), Character.BLUE)
            );
        });
    }

    @Override
    public void onGlobalConnectionEvent(GameSessionContext context, Channel playerChannel) {

        final var playerGameStateRequestQueue = playerChannel
                .attr(gameStateRequestQueueAttribute)
                .get();

        if (playerGameStateRequestQueue == null) {

            return;
        }

        if (playerGameStateRequestQueue.isEmpty()) {

            return;
        }

        final var gameStateRequest = playerGameStateRequestQueue.peek();
        final var gameCode = Objects.requireNonNullElse(gameStateRequest.gameCode(), "");

        if (!gameUUID.toString().substring(0, 8).equals(gameCode.substring(0, 8))) {

            logger.log(Level.FINEST, "Player sent non-matching game code: {0}", gameCode);

            return;
        }

        playerGameStateRequestQueue.poll();

        playerChannel
                .attr(playerPositionAttribute)
                .set(new int[]{xBound / 2, yBound / 2, 0});

        playerChannel
                .attr(playerNameAttribute)
                .set(gameStateRequest.player().name());

        playerNames[1] = playerChannel
                .attr(playerNameAttribute)
                .get();

        playerChannel
                .attr(playerCharacterAttribute)
                .set(gameStateRequest.player().character());

        context.registerPlayerConnection(playerChannel);

        logger.info("A player has joined");
    }

    @Override
    public void onInitialize(GameSessionContext context) {

        context.registerPlayerConnection(hostChannel);
    }

    @Override
    public void onServerTick(GameSessionContext context) {

        length = System.currentTimeMillis() - start; //todo: framework feature fix !!!! !!!!

        final var currentGameStateSum = new AtomicInteger(0);
        final var i = new AtomicInteger(0);

        context.performOnAllConnections(playerChannel -> {

            final var playerGameStateRequestQueue = playerChannel
                    .attr(gameStateRequestQueueAttribute)
                    .get();

            if (playerGameStateRequestQueue == null) {

                return;
            }

            final var gameStateRequest = playerGameStateRequestQueue.poll();

            if (gameStateRequest == null) {

                orderedPlayerChannels[i.get()] = playerChannel;

                i.set(i.incrementAndGet());

                return;
            }

            switch (gameStateRequest.gameStatus()) {

                case GameStatus.STOP_SESSION -> {

                    playerChannel
                            .attr(playerPositionAttribute)
                            .set(null);

                    playerChannel
                            .attr(playerNameAttribute)
                            .set(null);

                    playerChannel
                            .attr(gameStateRequestQueueAttribute)
                            .set(null);

                    context.unregisterPlayerConnection(playerChannel);

                    logger.info("A player has left");
                }

                case GameStatus.STATE_CHANGE -> {

                    final var currentPlayerState = playerChannel
                            .attr(playerPositionAttribute)
                            .get();

                    if (Objects.isNull(currentPlayerState)) {

                        logger.log(Level.WARNING, "Player with no state {0}", playerChannel);

                        return;
                    }

                    if (gameStateRequest.player().position().x() > xBound || gameStateRequest.player().position().y() > yBound) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    if (gameStateRequest.player().position().x() < 0 || gameStateRequest.player().position().y() < 0) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    if (
                            gameStateRequest.player().position().rotation() != 0
                                    && gameStateRequest.player().position().rotation() != 90
                                    && gameStateRequest.player().position().rotation() != 180
                                    && gameStateRequest.player().position().rotation() != 270
                    ) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    final var xDelta = Math.abs(gameStateRequest.player().position().x() - currentPlayerState[0]);
                    final var yDelta = Math.abs(gameStateRequest.player().position().y() - currentPlayerState[1]);

                    if ((xDelta != moveDelta && xDelta != 0) || (yDelta != 0 && yDelta != moveDelta)) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    playerChannel
                            .attr(playerPositionAttribute)
                            .set(new int[]{(int) gameStateRequest.player().position().x(), (int) gameStateRequest.player().position().y(), gameStateRequest.player().position().rotation()});

                    orderedPlayerChannels[i.get()] = playerChannel;

                    i.set(i.incrementAndGet());
                }
            }
        });

        context.performOnAllConnections(playerChannel -> {

            final var currentPlayerState = playerChannel
                    .attr(playerPositionAttribute)
                    .get();

            if (Objects.isNull(currentPlayerState)) {

                logger.log(Level.WARNING, "Player with no state {0}", playerChannel);

                currentGameStateSum.set(-1);

                return;
            }

            currentGameStateSum.set(currentGameStateSum.addAndGet(Arrays.stream(currentPlayerState).sum()));
        });

        if (currentGameStateSum.get() < 0 || lastGameHash == Integer.hashCode(currentGameStateSum.get())) {

            return;
        }

        lastGameHash = Integer.hashCode(currentGameStateSum.get());

        if (orderedPlayerChannels[0] == null) {

            logger.fine("No channels to send game state to");

            return;
        }

        sendGameStateToOrderedPlayerChannels(GameStatus.STATE_CHANGE);
    }

    @Override
    public void onEnded(GameSessionContext context) {

        final var gameHistory = new GameHistoryEntity();

        gameHistory.setGameSessionUUID(gameUUID.toString());
        gameHistory.setEndTime(Timestamp.from(Instant.now()));
        gameHistory.setPlayer1Name(playerNames[0]);
        gameHistory.setPlayer2Name(playerNames[1]);
        gameHistory.setLength(length);

        final var storedGameHistoryOptional = gameHistories.store(gameHistory);

        storedGameHistoryOptional
                .ifPresent(storedGameHistory -> logger.log(Level.INFO, "Persisted game history, and ended game session, game history: \n{0}", storedGameHistory));
    }

    @Override
    public void onContextConnectionsEmpty(GameSessionContext context) {

        logger.fine("Context connections empty, sending STOP_SESSION");

        context.broadcast(new GameStateFlatBuffersSerializable()
                .withGameState(length, GameStatus.STOP_SESSION, "")
        );
    }

    @Override
    public void onContextConnection(GameSessionContext context, Channel playerChannel) {

        context.performOnAllConnections(channel -> {

            if (channel == playerChannel) {

                orderedPlayerChannels[0] = channel;
                /*playerNames[0] = orderedPlayerChannels[0]
                        .attr(playerNameAttribute)
                        .get();*/

                return;
            }

            orderedPlayerChannels[1] = channel;
            /*playerNames[1] = orderedPlayerChannels[1]
                    .attr(playerNameAttribute)
                    .get();*/
        });

        sendGameStateToOrderedPlayerChannels(GameStatus.JOIN_SESSION);
    }

    @Override
    public void onContextConnectionClosed(GameSessionContext context) {

        logger.finest("Got context connection closed");
    }

    @Override
    public void onContextConnectionCountChanged(GameSessionContext context) {

        logger.finest("Got context connection count changed");
    }

    private void sendInvalid(Channel channel) {

        channel.writeAndFlush(
                new GameStateFlatBuffersSerializable()
                        .ofInvalidStatus()
        );
    }

    private void sendGameStateToOrderedPlayerChannels(byte gameStatus) {

        logger.log(Level.FINE, "Sending game state to ordered player channels {0}", Arrays.toString(orderedPlayerChannels));

        if (orderedPlayerChannels[1] == null) {

            orderedPlayerChannels[0].writeAndFlush(new GameStateFlatBuffersSerializable()
                    .withGameState(length, gameStatus, "")
                    .withPlayer(
                            orderedPlayerChannels[0].attr(playerPositionAttribute).get(),
                            orderedPlayerChannels[0].attr(playerNameAttribute).get(),
                            orderedPlayerChannels[0].attr(playerCharacterAttribute).get()
                    ));

            return;
        }

        orderedPlayerChannels[0].writeAndFlush(new GameStateFlatBuffersSerializable()
                .withGameState(length, gameStatus, "")
                .withPlayer(
                        orderedPlayerChannels[0].attr(playerPositionAttribute).get(),
                        orderedPlayerChannels[0].attr(playerNameAttribute).get(),
                        orderedPlayerChannels[0].attr(playerCharacterAttribute).get()
                )
                .withPlayer(
                        orderedPlayerChannels[1].attr(playerPositionAttribute).get(),
                        orderedPlayerChannels[1].attr(playerNameAttribute).get(),
                        orderedPlayerChannels[1].attr(playerCharacterAttribute).get()
                )
        );

        orderedPlayerChannels[1].writeAndFlush(new GameStateFlatBuffersSerializable()
                .withGameState(length, gameStatus, gameUUID.toString().substring(0, 8))
                .withPlayer(
                        orderedPlayerChannels[1].attr(playerPositionAttribute).get(),
                        orderedPlayerChannels[1].attr(playerNameAttribute).get(),
                        orderedPlayerChannels[1].attr(playerCharacterAttribute).get()
                )
                .withPlayer(
                        orderedPlayerChannels[0].attr(playerPositionAttribute).get(),
                        orderedPlayerChannels[0].attr(playerNameAttribute).get(),
                        orderedPlayerChannels[0].attr(playerCharacterAttribute).get()
                )
        );
    }
}
