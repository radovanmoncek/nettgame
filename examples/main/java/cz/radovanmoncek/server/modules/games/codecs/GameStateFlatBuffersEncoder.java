package cz.radovanmoncek.server.modules.games.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.modules.games.models.GameStateFlatBufferSerializable;
import cz.radovanmoncek.server.ship.compiled.schemas.Game;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.server.ship.compiled.schemas.Player;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;

/**
 * Example.
 */
public final class GameStateFlatBuffersEncoder extends FlatBuffersEncoder<GameStateFlatBufferSerializable> {

    @Override
    protected byte[] encodeHeader(GameStateFlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {
        return new byte[]{'G'};
    }

    @Override
    protected byte [] encodeBodyAfterHeader(final GameStateFlatBufferSerializable gameState, final FlatBufferBuilder builder) {

        return gameState.serialize(builder);
    }
}
