package container.game.docker.modules.examples.games.codecs;

import container.game.docker.ship.examples.compiled.schemas.GameStateRequest;
import container.game.docker.ship.parents.codecs.FlatBuffersDecoder;

import java.nio.ByteBuffer;

public final class GameStateRequestFlatBuffersDecoder extends FlatBuffersDecoder<GameStateRequest> {

    public GameStateRequestFlatBuffersDecoder() {

        super(GameStateRequest.class);
    }

    @Override
    protected GameStateRequest decodeBodyAfterHeader(final ByteBuffer in) {

        return GameStateRequest.getRootAsGameStateRequest(in);
    }
}
