package cz.radovanmoncek.server.modules.games.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;

/**
 * "Band-aid" class for FlatBuffers Schema.
 */
public record GameStateFlatBufferSerializable(Game game, Player[] players) implements FlatBufferSerializable {

    @Override
    public byte [] serialize(FlatBufferBuilder builder) {

        if(game.gameStatus == GameStatus.INVALID_STATE || game.gameStatus == GameStatus.STOP_SESSION) {

            final var game = cz.radovanmoncek.server.ship.compiled.schemas.Game.createGame(builder, this.game.gameStatus, 0);

            builder.finish(GameState.createGameState(builder, game, 0, 0));

            return builder.sizedByteArray();
        }

        final int gameCode = builder.createString(game.gameCode);
        final var firstPlayer = players()[0];
        final var secondPlayer = players()[1];
        final var firstPlayerX = firstPlayer.x();
        final var firstPlayerY = firstPlayer.y();
        final var firstPlayerRotationAngle = firstPlayer.rotationAngle();
        final int firstPlayerName = builder.createString(firstPlayer.name);
        final var encodedFirstPlayer = cz.radovanmoncek.server.ship.compiled.schemas.Player.createPlayer(builder, firstPlayerX, firstPlayerY, firstPlayerRotationAngle, firstPlayerName);
        int encodedSecondPlayer = 0;

        if(secondPlayer != null) {
            final var secondPlayerX = secondPlayer.x;
            final var secondPlayerY = secondPlayer.y;
            final var secondPlayerRotationAngle = secondPlayer.rotationAngle;
            final int secondPlayerName = builder.createString(secondPlayer.name);

            encodedSecondPlayer = cz.radovanmoncek.server.ship.compiled.schemas.Player.createPlayer(builder, secondPlayerX, secondPlayerY, secondPlayerRotationAngle, secondPlayerName);
        }

        final var game = cz.radovanmoncek.server.ship.compiled.schemas.Game.createGame(builder, this.game.gameStatus, gameCode);

        GameState.startGameState(builder);
        GameState.addGame(builder, game);

        switch (this.game.gameStatus) {

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

    public record Game(byte gameStatus, String gameCode) {}

    public record Player(int x, int y, int rotationAngle, String name) {}
}
