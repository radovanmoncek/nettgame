package cz.radovanmoncek.server.modules.games.events;

import cz.radovanmoncek.server.modules.games.models.GameStateFlatBufferSerializable;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.ship.parents.sessions.listeners.GameSessionEventListener;
import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ExampleGameSessionEventListener extends GameSessionEventListener {
    private static final Logger logger = LogManager.getLogger(ExampleGameSessionEventListener.class);
    private static final AttributeKey<ConcurrentLinkedQueue<GameStateRequest>> gameStateRequestQueueAttribute = AttributeKey.valueOf("gameStateRequestQueue");
    private static final AttributeKey<GameStateFlatBufferSerializable.Player> playerStateAttribute = AttributeKey.valueOf("playerState");
    private static final int xBound = 2000, yBound = 2000, moveDelta = 8;
    private final UUID gameUUID;
    private final Channel hostChannel;
    private int lastGameSum = 0;

    public ExampleGameSessionEventListener(Channel hostChannel) {

        gameUUID = UUID.randomUUID();
        this.hostChannel = hostChannel;
    }

    @Override
    public void onErrorThrown(final GameSessionContext context, Throwable throwable) {

        logger.error(throwable.getMessage(), throwable);
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
                    .attr(playerStateAttribute)
                    .set(new GameStateFlatBufferSerializable.Player(xBound / 2, yBound / 2, 0, gameStateRequest.name()));

            playerChannel.writeAndFlush(
                    new GameStateFlatBufferSerializable(
                            new GameStateFlatBufferSerializable.Game(GameStatus.START_SESSION, gameUUID.toString().substring(0, 8)),
                            new GameStateFlatBufferSerializable.Player[]{
                                    playerChannel.attr(playerStateAttribute).get(),
                                    null
                            }
                    )
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

        final var gameStateRequest = playerGameStateRequestQueue.poll();
        final var gameCode = Objects.requireNonNullElse(gameStateRequest.gameCode(), "");

        if (!gameUUID.toString().substring(0, 8).equals(gameCode.substring(0, 8))) {

            sendInvalid(playerChannel);

            return;
        }

        playerChannel
                .attr(playerStateAttribute)
                .set(new GameStateFlatBufferSerializable.Player(xBound / 2, yBound / 2, 0, gameStateRequest.name()));

        context.registerPlayerConnection(playerChannel);

        logger.info("A player has joined");
    }

    @Override
    public void onInitialize(GameSessionContext context) {

        context.registerPlayerConnection(hostChannel);
    }

    @Override
    public void onRunning(GameSessionContext context) {

        final var orderedPlayerChannels = new Channel[2];
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

            switch (gameStateRequest.gameStatusRequest()) {

                case GameStatus.STOP_SESSION -> {

                    playerChannel
                            .attr(playerStateAttribute)
                            .set(null);

                    playerChannel
                            .attr(gameStateRequestQueueAttribute)
                            .set(null);

                    context.unregisterPlayerConnection(playerChannel);

                    logger.info("A player has left");
                }

                case GameStatus.STATE_CHANGE -> {

                    GameStateFlatBufferSerializable.Player currentPlayerState = playerChannel
                            .attr(playerStateAttribute)
                            .get();

                    if (Objects.isNull(currentPlayerState)) {
                        return;
                    }

                    if (gameStateRequest.x() > xBound || gameStateRequest.y() > yBound) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    if (gameStateRequest.x() < 0 || gameStateRequest.y() < 0) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    if (
                            gameStateRequest.rotationAngle() != 0
                                    && gameStateRequest.rotationAngle() != 90
                                    && gameStateRequest.rotationAngle() != 180
                                    && gameStateRequest.rotationAngle() != 270
                    ) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    final var xDelta = Math.abs(gameStateRequest.x() - currentPlayerState.x());
                    final var yDelta = Math.abs(gameStateRequest.y() - currentPlayerState.y());

                    if ((xDelta != moveDelta && xDelta != 0) || (yDelta != 0 && yDelta != moveDelta)) {

                        sendInvalid(playerChannel);

                        return;
                    }

                    playerChannel
                            .attr(playerStateAttribute)
                            .set(new GameStateFlatBufferSerializable.Player(gameStateRequest.x(), gameStateRequest.y(), gameStateRequest.rotationAngle(), currentPlayerState.name()));

                    currentPlayerState = playerChannel
                            .attr(playerStateAttribute)
                            .get();

                    currentGameStateSum.set(currentGameStateSum.get()
                            + currentPlayerState.x()
                            + currentPlayerState.y()
                            + currentPlayerState.rotationAngle()
                    );

                    orderedPlayerChannels[i.get()] = playerChannel;

                    i.set(i.incrementAndGet());
                }
            }
        });

        if (lastGameSum == currentGameStateSum.get()) {

            return;
        }

        lastGameSum = currentGameStateSum.get();

        if(orderedPlayerChannels[0] == null)
            return;

        orderedPlayerChannels[0].writeAndFlush(
                new GameStateFlatBufferSerializable(
                        new GameStateFlatBufferSerializable.Game(GameStatus.STATE_CHANGE, ""),
                        new GameStateFlatBufferSerializable.Player[]{
                                orderedPlayerChannels[0].attr(playerStateAttribute).get(),
                                orderedPlayerChannels[1] == null ? null : orderedPlayerChannels[1].attr(playerStateAttribute).get()
                        }
                )
        );

        if(orderedPlayerChannels[1] == null)
            return;

        orderedPlayerChannels[1].writeAndFlush(
                new GameStateFlatBufferSerializable(
                        new GameStateFlatBufferSerializable.Game(GameStatus.STATE_CHANGE, ""),
                        new GameStateFlatBufferSerializable.Player[]{
                                orderedPlayerChannels[1].attr(playerStateAttribute).get(),
                                orderedPlayerChannels[0].attr(playerStateAttribute).get()
                        }
                )
        );
    }

    @Override
    public void onEnded(GameSessionContext context) {

    }

    @Override
    public void onContextConnectionsEmpty(GameSessionContext context) {

        context.broadcast(new GameStateFlatBufferSerializable(new GameStateFlatBufferSerializable.Game(GameStatus.STOP_SESSION, gameUUID.toString().substring(0, 8)), null));
    }

    @Override
    public void onContextConnection(GameSessionContext context, Channel playerChannel) {

        final var orderedPlayerChannels = new Channel[2];

        context.performOnAllConnections(channel -> {

            if (channel == playerChannel) {

                orderedPlayerChannels[0] = channel;

                return;
            }

            orderedPlayerChannels[1] = channel;
        });

        if(orderedPlayerChannels[0] == null)
            return;

        orderedPlayerChannels[0].writeAndFlush(new GameStateFlatBufferSerializable(
                new GameStateFlatBufferSerializable.Game(GameStatus.JOIN_SESSION, gameUUID.toString().substring(0, 8)),
                new GameStateFlatBufferSerializable.Player[]{
                        orderedPlayerChannels[0].attr(playerStateAttribute).get(),
                        orderedPlayerChannels[1].attr(playerStateAttribute).get()
                }));

        if(orderedPlayerChannels[1] == null)
            return;

        orderedPlayerChannels[1].writeAndFlush(new GameStateFlatBufferSerializable(
                new GameStateFlatBufferSerializable.Game(GameStatus.JOIN_SESSION, gameUUID.toString().substring(0, 8)),
                new GameStateFlatBufferSerializable.Player[]{
                        orderedPlayerChannels[1].attr(playerStateAttribute).get(),
                        orderedPlayerChannels[0].attr(playerStateAttribute).get()
                }));
    }

    @Override
    public void onContextConnectionClosed(GameSessionContext context) {

    }

    @Override
    public void onContextConnectionCountChanged(GameSessionContext context) {

    }

    private void sendInvalid(Channel channel) {

        channel.writeAndFlush(
                new GameStateFlatBufferSerializable(
                        new GameStateFlatBufferSerializable.Game(GameStatus.INVALID_STATE, ""),
                        new GameStateFlatBufferSerializable.Player[]{
                                new GameStateFlatBufferSerializable.Player(0, 0, 0, ""),
                                new GameStateFlatBufferSerializable.Player(0, 0, 0, "")
                        }
                )
        );
    }
}
