package cz.radovanmoncek.modules.games.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
import cz.radovanmoncek.ship.tables.*;

import java.util.Arrays;
import java.util.Objects;

/**
 * "Band-aid" class for FlatBuffers Schema.
 */
public class GameStateFlatBuffersSerializable implements FlatBufferSerializable {
    private final int[][] playerPositions;
    private final int[][] bulletPositions;
    private final String[] playerNames;
    private final Byte[] characters;
    private byte gameStatus;
    private String gameCode;
    private long length;

    public GameStateFlatBuffersSerializable() {

        playerPositions = new int[2][];
        playerNames = new String[2];
        //200 is the maximum bullet entity count
        bulletPositions = new int[200][];
        characters = new Byte[2];
    }

    public GameStateFlatBuffersSerializable withPlayer(int[] playerPosition, String playerName, byte character) {

        for (var i = 0; i < playerPositions.length; i++) {

            if (playerPositions[i] != null)
                continue;

            playerPositions[i] = playerPosition;

            break;
        }

        for (var i = 0; i < playerNames.length; i++) {

            if (playerNames[i] != null)
                continue;

            playerNames[i] = playerName;

            break;
        }

        for (var i = 0; i < characters.length; i++) {

            if (characters[i] != null)
                continue;

            characters[i] = character;

            break;
        }

        return this;
    }

    public GameStateFlatBuffersSerializable withBullet(int[] bulletPosition) {

        for (var i = 0; i < bulletPositions.length; i++) {

            if (bulletPositions[i] != null)
                continue;

            bulletPositions[i] = bulletPosition;
        }

        return this;
    }

    public GameStateFlatBuffersSerializable withGameState(long length, byte gameStatus, String gameCode) {

        this.length = length;
        this.gameStatus = gameStatus;
        this.gameCode = gameCode;

        return this;
    }

    @Override
    public byte[] serialize(FlatBufferBuilder builder) {

        final var gameCode = builder.createString(this.gameCode);
        final var game = GameMetadata.createGameMetadata(builder, gameStatus, gameCode, length);
        final var nonNullPlayerLength = Arrays.stream(playerPositions).filter(Objects::nonNull).toArray().length;
        final var playerVector = new int[nonNullPlayerLength];

        for (var i = 0; i < nonNullPlayerLength; i++) {

            final var playerName = builder.createString(playerNames[i]);

            Player.startPlayer(builder);
            Player.addName(builder, playerName);
            Player.addPosition(builder, Position.createPosition(builder, playerPositions[i][0], playerPositions[i][1], playerPositions[i][2]));
            Player.addCharacter(builder, characters[i]);

            playerVector[i] = Player.endPlayer(builder);
        }

        final var playerVectorSerialised = GameState.createPlayersVector(builder, playerVector);

        GameState.startGameState(builder);
        GameState.addGameMetadata(builder, game);
        GameState.addPlayers(builder, playerVectorSerialised);

        final var encodedGameState = GameState.endGameState(builder);

        builder.finish(encodedGameState);

        return builder.sizedByteArray();
    }

    public GameStateFlatBuffersSerializable ofInvalidStatus() {

        gameStatus = GameStatus.INVALID_STATE;
        gameCode = "";
        length = 0;

        return this;
    }
}
