package container.game.docker.modules.examples.games.listeners;

import container.game.docker.modules.examples.games.models.GameStateFlatBufferSerializable;
import container.game.docker.ship.examples.compiled.schemas.GameStatus;
import container.game.docker.ship.examples.compiled.schemas.Player;
import container.game.docker.ship.parents.listeners.GameSessionListener;
import container.game.docker.ship.parents.models.GameSessionContext;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExampleGameSessionListener extends GameSessionListener {
    private static final Logger logger = LogManager.getLogger(ExampleGameSessionListener.class);
    private final ConcurrentHashMap<String, UUID> lobbyCodes;
    private int lastGameSum = 0;

    public ExampleGameSessionListener(ConcurrentHashMap<String, UUID> lobbyCodes) {

        this.lobbyCodes = lobbyCodes;
    }

    @Override
    public void onSessionErrorThrown(final GameSessionContext context, Throwable throwable) {

        logger.error(throwable.getMessage(), throwable);
    }

    @Override
    public void onSessionStart(final GameSessionContext context) {

        final var gameSessionUUIDOptional = context.retrieveGameSessionUUID();
        final var gameSessionPlayerChannels = context.retrievePlayerChannels();

        if (gameSessionUUIDOptional.isEmpty() || gameSessionPlayerChannels.isEmpty()) {

            return;
        }

        lobbyCodes.put(gameSessionUUIDOptional.get().toString().substring(0, 8), gameSessionUUIDOptional.get());

        final var hostPlayerStateAttribute = gameSessionPlayerChannels
                .getFirst()
                .attr(AttributeKey.valueOf("playerState"));

        if (hostPlayerStateAttribute.get() == null) {

            return;
        }

        gameSessionPlayerChannels
                .getFirst()
                .attr(AttributeKey.valueOf("gameSessionUUID"))
                .set(gameSessionUUIDOptional.get());

        context.broadcast(
                new GameStateFlatBufferSerializable(
                        new GameStateFlatBufferSerializable.Game(GameStatus.START_SESSION, gameSessionUUIDOptional.get().toString().substring(0, 8)),
                        new GameStateFlatBufferSerializable.Player[] {(GameStateFlatBufferSerializable.Player) gameSessionPlayerChannels.getFirst().attr(AttributeKey.valueOf("playerState")).get()}
                )
        );
    }

    @Override
    public void onSessionRunning(GameSessionContext context) {

        final var playerChannels = context.retrievePlayerChannels();
        final var playerStatesToBroadcast = new GameStateFlatBufferSerializable.Player[2];
        final var gameSessionUUIDOptional = context.retrieveGameSessionUUID();

        if (gameSessionUUIDOptional.isEmpty()) {

            return;
        }

        int currentGameStateSum = 0;

        if(playerChannels.size() == 1) {

            final var playerState = (GameStateFlatBufferSerializable.Player) playerChannels
                    .getFirst()
                    .attr(AttributeKey.valueOf("playerState"))
                    .get();

            currentGameStateSum += playerState.x() + playerState.y() + playerState.rotationAngle();

            playerStatesToBroadcast[0] = playerState;
        }

        if(playerChannels.size() > 1) {

            final var playerState = (GameStateFlatBufferSerializable.Player) playerChannels
                    .getFirst()
                    .attr(AttributeKey.valueOf("playerState"))
                    .get();

            currentGameStateSum += playerState.x() + playerState.y() + playerState.rotationAngle();

            playerStatesToBroadcast[1] = playerState;
        }

        if(lastGameSum == currentGameStateSum) {

            return;
        }

        context.broadcast(new GameStateFlatBufferSerializable(new GameStateFlatBufferSerializable.Game(GameStatus.STATE_CHANGE, gameSessionUUIDOptional.get().toString().substring(0, 8)), playerStatesToBroadcast));
    }

    @Override
    public void onSessionEnded(GameSessionContext context) {

        context.broadcast(new GameStateFlatBufferSerializable(new GameStateFlatBufferSerializable.Game(GameStatus.STOP_SESSION, context.retrieveGameSessionUUID().toString().substring(0, 8)), null));
    }
}
