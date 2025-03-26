package cz.radovanmoncek.server.modules.games.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.ship.tables.GameState;
import cz.radovanmoncek.server.ship.tables.Player;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;

import java.util.Objects;

/**
 * "Band-aid" class for FlatBuffers Schema.
 */
public class GameStateFlatBuffersSerializable implements FlatBufferSerializable {
    private byte gameStatus;
    private int[] player1Position;
    private int[] player2Position;
    private String name1 = "";
    private String name2 = "";
    private String gameCode = "";

    public GameStateFlatBuffersSerializable withName1(String name) {
        this.name1 = name;
        return this;
    }

    public GameStateFlatBuffersSerializable withName2(String name) {
        this.name2 = name;
        return this;
    }

    public GameStateFlatBuffersSerializable withPlayer1Position(int[] player1Position) {
        this.player1Position = player1Position;
        return this;
    }

    public GameStateFlatBuffersSerializable withPlayer2Position(int[] player2Position) {
        this.player2Position = player2Position;
        return this;
    }

    public GameStateFlatBuffersSerializable withGameStatus(byte gameStatus) {
        this.gameStatus = gameStatus;
        return this;
    }

    public GameStateFlatBuffersSerializable withGameCode(String gameCode) {

        this.gameCode = gameCode;
        return this;
    }

    @Override
    public byte[] serialize(FlatBufferBuilder builder) {

        final var gameCode = builder.createString(this.gameCode);
        final var game = cz.radovanmoncek.server.ship.tables.Game.createGame(builder, gameStatus, gameCode);
        final var firstPlayerName = builder.createString(name1);
        final var firstPlayerOffset = (player1Position == null || player1Position.length == 0)? 0 : Player.createPlayer(builder, player1Position[0], player1Position[1], player1Position[2], firstPlayerName);
        final var secondPlayerName = builder.createString(name2);
        final var secondPlayerOffset = (player2Position == null || player2Position.length == 0)? 0 : Player.createPlayer(builder, player2Position[0], player2Position[1], player2Position[2], secondPlayerName);

        GameState.startGameState(builder);
        GameState.addGame(builder, game);

        if(player1Position != null)
            GameState.addPlayer1(builder, firstPlayerOffset);

        if(player2Position != null)
            GameState.addPlayer2(builder, secondPlayerOffset);

        final var encodedGameState = GameState.endGameState(builder);

        builder.finish(encodedGameState);

        return builder.sizedByteArray();
    }
}
