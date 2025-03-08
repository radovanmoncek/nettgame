package cz.radovanmoncek.client.modules.games.codecs;

import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersDecoder;

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
