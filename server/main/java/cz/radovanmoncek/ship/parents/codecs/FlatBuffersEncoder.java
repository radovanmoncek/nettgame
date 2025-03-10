package cz.radovanmoncek.ship.parents.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Structure:
 * </p>
 * <pre>
 *  ---------------------------------------------------------
 *  |         Length 0x8    |        your header            |
 *  ---------------------------------------------------------
 *  |                        your body                      |
 *  ---------------------------------------------------------
 * </pre>
 * @param <BandAid> a class that is able to produce FlatBuffers encoded data of its attributes.
 */
public abstract class FlatBuffersEncoder<BandAid extends FlatBufferSerializable> extends MessageToByteEncoder<BandAid> {
    private static final Logger logger = LogManager.getLogger(FlatBuffersEncoder.class);

    @Override
    protected void encode(final ChannelHandlerContext channelHandlerContext, final BandAid flatBuffersSerializable, final ByteBuf out) {

        try {

            logger.info("Encoding {}", flatBuffersSerializable);

            final var builder = new FlatBufferBuilder(1024);
            final var header = encodeHeader(flatBuffersSerializable, builder);

            final var body = encodeBodyAfterHeader(flatBuffersSerializable, builder);

            logger.info("\n| {}B | {} |\n| {} |", header.length + body.length, header, body);

            out
                    .writeLong(header.length + body.length)
                    .writeBytes(header)
                    .writeBytes(body);
        }
        catch (final Exception exception) {

            logger.error(exception.getMessage(), exception);
        }
    }

    protected abstract byte [] encodeHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);

    protected abstract byte [] encodeBodyAfterHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);
}
