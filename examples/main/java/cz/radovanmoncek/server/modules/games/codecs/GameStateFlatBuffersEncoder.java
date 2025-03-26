package cz.radovanmoncek.server.modules.games.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;

/**
 * Encoder for {@link GameStateFlatBuffersSerializable}.
 */
public final class GameStateFlatBuffersEncoder extends FlatBuffersEncoder<GameStateFlatBuffersSerializable> {

    @Override
    protected byte[] encodeHeader(GameStateFlatBuffersSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {

        return new byte[]{'G'};
    }

    @Override
    protected byte [] encodeBodyAfterHeader(final GameStateFlatBuffersSerializable gameState, final FlatBufferBuilder builder) {

        return gameState.serialize(builder);
    }
}
