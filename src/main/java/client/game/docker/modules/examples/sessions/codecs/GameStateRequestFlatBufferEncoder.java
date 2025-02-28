package client.game.docker.modules.examples.sessions.codecs;

import client.game.docker.modules.examples.sessions.models.GameStateRequestFlatBuffersSerializable;
import com.google.flatbuffers.FlatBufferBuilder;
import container.game.docker.ship.examples.compiled.schemas.GameStateRequest;
import container.game.docker.ship.examples.compiled.schemas.GameStatus;
import container.game.docker.ship.parents.codecs.FlatBuffersEncoder;
import io.netty.channel.ChannelHandlerContext;

public class GameStateRequestFlatBufferEncoder extends FlatBuffersEncoder<GameStateRequestFlatBuffersSerializable> {

    @Override
    protected byte[] encodeBodyAfterHeader(GameStateRequestFlatBuffersSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {

        int name;

        try {
            name = flatBufferBuilder.createString(flatBuffersSerializable.name());
        }
        catch (final Exception e) {
            throw e;
        }

        GameStateRequest.startGameStateRequest(flatBufferBuilder);
        GameStateRequest.addGameStatusRequest(flatBufferBuilder, flatBuffersSerializable.gameStatus());

        switch (flatBuffersSerializable.gameStatus()) {

            case GameStatus.STATE_CHANGE -> {

                GameStateRequest.addX(flatBufferBuilder, flatBuffersSerializable.x());
                GameStateRequest.addY(flatBufferBuilder, flatBuffersSerializable.y());
                GameStateRequest.addRotationAngle(flatBufferBuilder, flatBuffersSerializable.rotationAngle());
            }

            case GameStatus.START_SESSION -> {

                try {

                    GameStateRequest.addName(flatBufferBuilder, name);
                } catch (Exception exception) {

                    throw exception;
                }
            }

            case GameStatus.JOIN_SESSION -> {

                GameStateRequest.addName(flatBufferBuilder, flatBufferBuilder.createString(flatBuffersSerializable.name()));
                GameStateRequest.addGameCode(flatBufferBuilder, flatBufferBuilder.createString(flatBuffersSerializable.gameCode()));
            }
        }

        final int gameStateRequest = GameStateRequest.endGameStateRequest(flatBufferBuilder);

        flatBufferBuilder.finish(gameStateRequest);

        return flatBufferBuilder.sizedByteArray();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        super.exceptionCaught(ctx, cause);
    }
}
