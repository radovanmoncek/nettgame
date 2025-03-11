package cz.radovanmoncek.server.modules.games.codecs;

import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersDecoder;

import java.nio.ByteBuffer;

public final class GameStateRequestFlatBuffersDecoder extends FlatBuffersDecoder<GameStateRequest> {

    @Override
    protected boolean decodeHeader(ByteBuffer in) {

        return in.get() == 'g';
    }

    @Override
    protected GameStateRequest decodeBodyAfterHeader(final ByteBuffer in) {

        return GameStateRequest.getRootAsGameStateRequest(in);
    }
}
