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

        logger.info("Encoding {}", flatBuffersSerializable);

        return flatBuffersSerializable.serialize(flatBufferBuilder);
    }

    @Override
    protected byte[] encodeHeader(GameStateRequestFlatBuffersSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {
        return new byte[]{'g'};
    }
}
