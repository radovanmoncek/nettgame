package client.game.docker.modules.examples.sessions.codecs;

import container.game.docker.ship.examples.compiled.schemas.GameState;
import container.game.docker.ship.parents.codecs.FlatBuffersDecoder;

import java.nio.ByteBuffer;

public class GameStateFlatBufferDecoder extends FlatBuffersDecoder<GameState> {

    public GameStateFlatBufferDecoder() {

        super(GameState.class);
    }

    @Override
    protected GameState decodeBodyAfterHeader(ByteBuffer in) {

        return GameState.getRootAsGameState(in);
    }
}
