package cz.radovanmoncek.ship.parents.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.injection.annotations.InjectEncoderBindings;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 *
 * @param <BandAid>
 */
public abstract class FlatBuffersEncoder<BandAid extends FlatBufferSerializable<?>> extends MessageToByteEncoder<BandAid> {
    private static final Logger logger = LogManager.getLogger(FlatBuffersEncoder.class);
    @SuppressWarnings("unused")
    @InjectEncoderBindings
    private Map<Class<?>, Byte> flatBuffersSchemaToMagicByteBindings;

    @Override
    protected void encode(final ChannelHandlerContext channelHandlerContext, final BandAid flatBuffersSerializable, final ByteBuf out) {

        out.writeByte(flatBuffersSchemaToMagicByteBindings.get(flatBuffersSerializable.getSchemaClass()));

        try {

            final var body = encodeBodyAfterHeader(flatBuffersSerializable, new FlatBufferBuilder(1024));

            out
                    .writeLong(body.length)
                    .writeBytes(body);
        }
        catch (final Exception exception) {

            logger.error(exception.getMessage(), exception);
        }
    }

    protected abstract byte [] encodeBodyAfterHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);
}
