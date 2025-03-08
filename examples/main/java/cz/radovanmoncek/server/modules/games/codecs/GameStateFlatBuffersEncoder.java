package cz.radovanmoncek.server.modules.games.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.modules.games.models.GameStateFlatBufferSerializable;
import cz.radovanmoncek.server.ship.compiled.schemas.Game;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.server.ship.compiled.schemas.Player;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;

/**
 * Example.
 */
public final class GameStateFlatBuffersEncoder extends FlatBuffersEncoder<GameStateFlatBufferSerializable> {

    @Override
    protected byte [] encodeBodyAfterHeader(final GameStateFlatBufferSerializable gameState, final FlatBufferBuilder builder) {

        if(gameState.game().gameStatus() == GameStatus.INVALID_STATE || gameState.game().gameStatus() == GameStatus.STOP_SESSION) {

            final var game = Game.createGame(builder, gameState.game().gameStatus(), 0);

            builder.finish(GameState.createGameState(builder, game, 0, 0));

            return builder.sizedByteArray();
        }

        final int gameCode = builder.createString(gameState.game().gameCode());
        final var firstPlayer = gameState.players()[0];
        final var secondPlayer = gameState.players()[1];
        final var firstPlayerX = firstPlayer.x();
        final var firstPlayerY = firstPlayer.y();
        final var firstPlayerRotationAngle = firstPlayer.rotationAngle();
        final int firstPlayerName = builder.createString(firstPlayer.name());
        final var encodedFirstPlayer = Player.createPlayer(builder, firstPlayerX, firstPlayerY, firstPlayerRotationAngle, firstPlayerName);
        int encodedSecondPlayer = 0;

        if(secondPlayer != null) {
            final var secondPlayerX = secondPlayer.x();
            final var secondPlayerY = secondPlayer.y();
            final var secondPlayerRotationAngle = secondPlayer.rotationAngle();
            final int secondPlayerName = builder.createString(secondPlayer.name());

            encodedSecondPlayer = Player.createPlayer(builder, secondPlayerX, secondPlayerY, secondPlayerRotationAngle, secondPlayerName);
        }

        final var game = Game.createGame(builder, gameState.game().gameStatus(), gameCode);

        GameState.startGameState(builder);
        GameState.addGame(builder, game);

        switch (gameState.game().gameStatus()) {

            case GameStatus.STATE_CHANGE, GameStatus.JOIN_SESSION -> {

                GameState.addPlayer1(builder, encodedFirstPlayer);
                GameState.addPlayer2(builder, encodedSecondPlayer);
            }

            case GameStatus.START_SESSION -> GameState.addPlayer1(builder, encodedFirstPlayer);
        }

        final var encodedGameState = GameState.endGameState(builder);

        builder.finish(encodedGameState);

        return builder.sizedByteArray();
    }
}
