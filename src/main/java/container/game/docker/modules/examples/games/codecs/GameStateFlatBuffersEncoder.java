package container.game.docker.modules.examples.games.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import container.game.docker.modules.examples.games.models.GameStateFlatBufferSerializable;
import container.game.docker.ship.examples.compiled.schemas.Game;
import container.game.docker.ship.examples.compiled.schemas.GameState;
import container.game.docker.ship.examples.compiled.schemas.GameStatus;
import container.game.docker.ship.examples.compiled.schemas.Player;
import container.game.docker.ship.parents.codecs.FlatBuffersEncoder;

/**
 * Example.
 */
public final class GameStateFlatBuffersEncoder extends FlatBuffersEncoder<GameStateFlatBufferSerializable> {

    @Override
    protected byte [] encodeBodyAfterHeader(final GameStateFlatBufferSerializable gameState, final FlatBufferBuilder builder) {

        final int gameCode = builder.createString(gameState.game().gameCode());
        final var firstPlayer = gameState.players()[0];
        final var secondPlayer = gameState.players()[1];
        final var firstPlayerX = firstPlayer.x();
        final var firstPlayerY = firstPlayer.y();
        final var firstPlayerRotationAngle = firstPlayer.rotationAngle();
        final int firstPlayerName = builder.createString(firstPlayer.name());
        final var encodedFirstPlayer = Player.createPlayer(builder, firstPlayerX, firstPlayerY, firstPlayerRotationAngle, firstPlayerName);
        final var secondPlayerX = secondPlayer.x();
        final var secondPlayerY = secondPlayer.y();
        final var secondPlayerRotationAngle = secondPlayer.rotationAngle();
        final int secondPlayerName = builder.createString(secondPlayer.name());
        final var encodedSecondPlayer = Player.createPlayer(builder, secondPlayerX, secondPlayerY, secondPlayerRotationAngle, secondPlayerName);

        GameState.startGameState(builder);
        GameState.addGame(builder, Game.createGame(builder, gameState.game().gameStatus(), gameCode));

        switch (gameState.game().gameStatus()) {

            case GameStatus.STATE_CHANGE -> {

                GameState.addPlayer1(builder, encodedFirstPlayer);
                GameState.addPlayer2(builder, encodedSecondPlayer);
            }

            case GameStatus.START_SESSION -> {

                GameState.addPlayer1(builder, encodedFirstPlayer);
            }
        }

        GameState.endGameState(builder);

        return builder.sizedByteArray();
    }
}
