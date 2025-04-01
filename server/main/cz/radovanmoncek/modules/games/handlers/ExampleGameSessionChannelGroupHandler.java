package cz.radovanmoncek.modules.games.handlers;

import cz.radovanmoncek.modules.games.events.ExampleGameSessionEventListener;
import cz.radovanmoncek.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.modules.games.repositories.GameHistories;
import cz.radovanmoncek.ship.tables.GameStatus;
import cz.radovanmoncek.ship.tables.Request;
import cz.radovanmoncek.ship.injection.annotations.ChannelHandlerAttributeInjectee;
import cz.radovanmoncek.ship.parents.handlers.GameSessionChannelGroupHandler;
import cz.radovanmoncek.ship.models.GameSessionConfigurationOption;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExampleGameSessionChannelGroupHandler extends GameSessionChannelGroupHandler<Request> {
    @SuppressWarnings("unused")
    @ChannelHandlerAttributeInjectee
    private GameHistories gameHistories;
    private static final Logger logger = Logger.getLogger(ExampleGameSessionChannelGroupHandler.class.getName());
    private static final AttributeKey<Queue<Request>> playerStateQueueAttribute = AttributeKey.valueOf("gameStateRequestQueue");
    private static final int maxNickNameLength = 8;

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final Request gameStateRequest) {

        final var playerChannel = channelHandlerContext.channel();

        switch (gameStateRequest.gameStatus()) {

            case GameStatus.START_SESSION -> {

                if (playerChannel
                        .attr(playerStateQueueAttribute)
                        .get() != null
                )
                    return;

                logger.log(Level.INFO, "Player {0} wants to start the game", playerChannel);

                final var requestedName = Objects.requireNonNullElse(gameStateRequest.player().name(), "");

                if (requestedName.length() > maxNickNameLength || requestedName.isBlank()) { //todo: auth module

                    playerChannel.writeAndFlush(
                            new GameStateFlatBuffersSerializable()
                                    .ofInvalidStatus()
                    );

                    logger.log(Level.INFO, "Invalid nickname {0} length {1} or format", new Object[]{requestedName, requestedName.length()});

                    return;
                }

                playerChannel
                        .attr(playerStateQueueAttribute)
                        .set(new ConcurrentLinkedQueue<>(List.of(gameStateRequest)));

                startGameSession(new ExampleGameSessionEventListener(playerChannel, gameHistories), List.of(new AbstractMap.SimpleEntry<>(GameSessionConfigurationOption.MAX_PLAYERS, 2)));
            }

            case GameStatus.STOP_SESSION, GameStatus.STATE_CHANGE -> {

                final var currentPlayerStateQueueAttribute = playerChannel
                        .attr(playerStateQueueAttribute);

                final var currentPlayerStateQueue = currentPlayerStateQueueAttribute.get();

                if(currentPlayerStateQueue == null)
                    return;

                currentPlayerStateQueue.offer(gameStateRequest);

                logger.fine("Game state request enqueued");
            }

            case GameStatus.JOIN_SESSION -> {

                final var currentPlayerStateQueueAttribute = playerChannel
                        .attr(playerStateQueueAttribute);

                final var currentPlayerStateQueue = currentPlayerStateQueueAttribute.get();

                if(currentPlayerStateQueue == null) {

                    currentPlayerStateQueueAttribute.set(new ConcurrentLinkedQueue<>(List.of(gameStateRequest)));

                    broadcastGlobalEvent(playerChannel);

                    return;
                }

                currentPlayerStateQueue.offer(gameStateRequest);

                broadcastGlobalEvent(playerChannel);
            }
        }
    }
}
