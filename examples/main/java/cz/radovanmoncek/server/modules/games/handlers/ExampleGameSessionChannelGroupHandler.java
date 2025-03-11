package cz.radovanmoncek.server.modules.games.handlers;

import cz.radovanmoncek.server.modules.games.events.ExampleGameSessionEventListener;
import cz.radovanmoncek.server.modules.games.models.GameStateFlatBufferSerializable;
import cz.radovanmoncek.server.modules.games.models.ExampleGameHistoryPersistableModel;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.ship.injection.annotations.AttributeInjectee;
import cz.radovanmoncek.ship.parents.handlers.GameSessionChannelGroupHandler;
import cz.radovanmoncek.ship.sessions.models.GameSessionConfigurationOption;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExampleGameSessionChannelGroupHandler extends GameSessionChannelGroupHandler<GameStateRequest> {
    @SuppressWarnings("unused")
    @AttributeInjectee
    private ExampleGameHistoryPersistableModel exampleGameHistoryPersistableModel;
    private static final Logger logger = LogManager.getLogger(ExampleGameSessionChannelGroupHandler.class);
    private static final AttributeKey<Queue<GameStateRequest>> playerStateQueueAttribute = AttributeKey.valueOf("gameStateRequestQueue");
    private static final int maxNickNameLength = 8;

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final GameStateRequest gameStateRequest) {

        final var playerChannel = channelHandlerContext.channel();

        switch (gameStateRequest.gameStatusRequest()) {

            case GameStatus.START_SESSION -> {

                if (playerChannel
                        .attr(playerStateQueueAttribute)
                        .get() != null
                )
                    return;

                logger.info("Player {} wants to start the game", playerChannel);

                final var requestedName = Objects.requireNonNullElse(gameStateRequest.name(), "");

                if (requestedName.length() > maxNickNameLength || requestedName.isBlank()) { //todo: auth module

                    playerChannel.writeAndFlush(new GameStateFlatBufferSerializable(new GameStateFlatBufferSerializable.Game(GameStatus.INVALID_STATE, ""), null));

                    logger.info("Invalid nickname {} length {} or format", requestedName, requestedName.length());

                    return;
                }

                playerChannel
                        .attr(playerStateQueueAttribute)
                        .set(new ConcurrentLinkedQueue<>(List.of(gameStateRequest)));

                startGameSession(new ExampleGameSessionEventListener(playerChannel, exampleGameHistoryPersistableModel), List.of(new AbstractMap.SimpleEntry<>(GameSessionConfigurationOption.MAX_PLAYERS, 2)));
            }

            case GameStatus.STOP_SESSION, GameStatus.STATE_CHANGE -> {

                final var currentPlayerStateQueueAttribute = playerChannel
                        .attr(playerStateQueueAttribute);

                final var currentPlayerStateQueue = currentPlayerStateQueueAttribute.get();

                if(currentPlayerStateQueue == null)
                    return;

                currentPlayerStateQueue.offer(gameStateRequest);
            }

            case GameStatus.JOIN_SESSION -> {

                final var currentPlayerStateQueueAttribute = playerChannel
                        .attr(playerStateQueueAttribute);

                final var currentPlayerStateQueue = currentPlayerStateQueueAttribute.get();

                if(currentPlayerStateQueue == null) {

                    currentPlayerStateQueueAttribute.set(new ConcurrentLinkedQueue<>(List.of(gameStateRequest)));

                    broadcastEvent(playerChannel);

                    return;
                }

                currentPlayerStateQueue.offer(gameStateRequest);

                broadcastEvent(playerChannel);
            }
        }
    }
}
