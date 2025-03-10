package cz.radovanmoncek.client.modules.games.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record GameStateRequestFlatBuffersSerializable(int x, int y, int rotationAngle, String name, byte gameStatus, String gameCode) implements FlatBufferSerializable {
    private static final Logger logger  = LogManager.getLogger(GameStateRequestFlatBuffersSerializable.class);

    @Override
    public byte[] serialize(FlatBufferBuilder builder) {

        if (gameCode == null || name == null){

            logger.error("Serializable {} invalid", this);

            return new byte[0];
        }

        final var name = builder.createString(this.name);
        final var gameCode = builder.createString(this.gameCode);

        GameStateRequest.startGameStateRequest(builder);
        GameStateRequest.addGameStatusRequest(builder, gameStatus);

        switch (gameStatus) {

            case GameStatus.STATE_CHANGE -> {

                GameStateRequest.addX(builder, x);
                GameStateRequest.addY(builder, y);
                GameStateRequest.addRotationAngle(builder, rotationAngle);
            }

            case GameStatus.START_SESSION -> GameStateRequest.addName(builder, name);

            case GameStatus.JOIN_SESSION -> {

                GameStateRequest.addName(builder, name);
                GameStateRequest.addGameCode(builder, gameCode);
            }
        }

        final var gameStateRequest = GameStateRequest.endGameStateRequest(builder);

        builder.finish(gameStateRequest);

        return builder.sizedByteArray();
    }
}
