package container.game.docker.modules.examples.games.handlers;

import container.game.docker.modules.examples.games.listeners.ExampleGameSessionListener;
import container.game.docker.modules.examples.games.models.GameStateFlatBufferSerializable;
import container.game.docker.ship.builders.GameSessionConfigurationBuilder;
import container.game.docker.ship.directors.GameSessionConfigurationDirector;
import container.game.docker.ship.examples.compiled.schemas.GameStateRequest;
import container.game.docker.ship.parents.handlers.GameSessionHandler;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static container.game.docker.ship.examples.compiled.schemas.GameStatus.*;

public class ExampleGameSessionHandler extends GameSessionHandler<GameStateRequest> {
    private static final Logger logger = LogManager.getLogger(ExampleGameSessionHandler.class);
    private static final int maxNickNameLength = 8, xBound = 2000, yBound = 2000, moveDelta = 8;
    private static final ConcurrentHashMap<String, UUID> gameCodes;
    private static final AttributeKey<GameStateFlatBufferSerializable.Player> playerState = AttributeKey.valueOf("playerState");

    static {

        gameCodes = new ConcurrentHashMap<>();
    }

    @Override
    protected void playerChannelRead(final GameStateRequest gameStateRequest, final Channel playerChannel) {

        if (Objects.nonNull(gameStateRequest.name()) && gameStateRequest.name().length() > maxNickNameLength) {

            sendInvalid(playerChannel);

            logger.info("Invalid nickname length {}", gameStateRequest.name().length());

            return;
        }

        switch (gameStateRequest.gameStatusRequest()) {

            case START_SESSION -> {

                logger.info("Player wants to start the game");

                playerChannel
                        .attr(playerState)
                        .set(new GameStateFlatBufferSerializable.Player(
                                xBound / 2,
                                yBound / 2,
                                0,
                                gameStateRequest.name()
                        ));

                final var gameSessionConfiguration = new GameSessionConfigurationDirector(new GameSessionConfigurationBuilder())
                        .make2PlayerGameSessionConfiguration()
                        .buildGameSessionUpdateListener(new ExampleGameSessionListener(gameCodes))
                        .buildHostChannel(playerChannel)
                        .build();

                startGameSession(gameSessionConfiguration);
            }

            case STOP_SESSION -> {

                logger.info("A player has requested session end {}", gameStateRequest);

                deregisterPlayerFromSessionConnections(playerChannel);
            }

            case JOIN_SESSION -> {

                final var gameSessionUUID = gameCodes.get(gameStateRequest.gameCode());

                if (gameSessionUUID == null) {

                    logger.info("Could not find session UUID for game code {}", gameStateRequest.gameCode());

                    sendInvalid(playerChannel);

                    return;
                }

                playerChannel
                        .attr(playerState)
                        .set(new GameStateFlatBufferSerializable.Player(xBound / 2, yBound / 2, 0, gameStateRequest.name()));

                registerPlayerConnectionEvent(gameSessionUUID, playerChannel);

                logger.info("A player has requested session join {}", gameSessionUUID);
            }

            case STATE_CHANGE -> {

                final var currentPlayerStateAttribute = playerChannel
                        .attr(playerState);

                if (currentPlayerStateAttribute == null)
                    return;

                final var currentPlayerState = currentPlayerStateAttribute.get();

                if (gameStateRequest.x() > xBound || gameStateRequest.y() > yBound) {

                    playerChannel.writeAndFlush(new GameStateFlatBufferSerializable(new GameStateFlatBufferSerializable.Game(INVALID_STATE, null), null));

                    return;
                }

                if (gameStateRequest.x() < 0 || gameStateRequest.y() < 0) {



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

                currentPlayerStateAttribute.set(new GameStateFlatBufferSerializable.Player(gameStateRequest.x(), gameStateRequest.y(), gameStateRequest.rotationAngle(), currentPlayerState.name()));
            }
        }
    }

    @Override
    protected void playerDisconnected(Channel playerChannel) {

//        playerChannel.
    }

    private void sendInvalid(Channel channel) {

        channel.writeAndFlush(new GameStateFlatBufferSerializable(new GameStateFlatBufferSerializable.Game(INVALID_STATE, ""), null));
    }
}
