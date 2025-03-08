package cz.radovanmoncek.client.modules.games.codecs;

import cz.radovanmoncek.client.modules.games.models.GameStateRequestFlatBuffersSerializable;
import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameStateRequestFlatBufferEncoder extends FlatBuffersEncoder<GameStateRequestFlatBuffersSerializable> {
    private static final Logger logger = LogManager.getLogger(GameStateRequestFlatBufferEncoder.class);

    @Override
    protected byte[] encodeBodyAfterHeader(GameStateRequestFlatBuffersSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {

        if (flatBuffersSerializable.gameCode() == null || flatBuffersSerializable.name() == null){

            logger.error("Serializable {} invalid", flatBuffersSerializable);

            return new byte[0];
        }

        final var name = flatBufferBuilder.createString(flatBuffersSerializable.name());
        final var gameCode = flatBufferBuilder.createString(flatBuffersSerializable.gameCode());

        GameStateRequest.startGameStateRequest(flatBufferBuilder);
        GameStateRequest.addGameStatusRequest(flatBufferBuilder, flatBuffersSerializable.gameStatus());

        switch (flatBuffersSerializable.gameStatus()) {

            case GameStatus.STATE_CHANGE -> {

                GameStateRequest.addX(flatBufferBuilder, flatBuffersSerializable.x());
                GameStateRequest.addY(flatBufferBuilder, flatBuffersSerializable.y());
                GameStateRequest.addRotationAngle(flatBufferBuilder, flatBuffersSerializable.rotationAngle());
            }

            case GameStatus.START_SESSION -> GameStateRequest.addName(flatBufferBuilder, name);

            case GameStatus.JOIN_SESSION -> {

                GameStateRequest.addName(flatBufferBuilder, name);
                GameStateRequest.addGameCode(flatBufferBuilder, gameCode);
            }
        }

        final var gameStateRequest = GameStateRequest.endGameStateRequest(flatBufferBuilder);

        flatBufferBuilder.finish(gameStateRequest);

        return flatBufferBuilder.sizedByteArray();
    }
}
