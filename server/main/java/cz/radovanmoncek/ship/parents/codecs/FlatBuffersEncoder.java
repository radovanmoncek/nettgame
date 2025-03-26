package cz.radovanmoncek.ship.parents.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(FlatBuffersEncoder.class.getName());

    @Override
    protected void encode(final ChannelHandlerContext channelHandlerContext, final BandAid flatBuffersSerializable, final ByteBuf out) {

        try {

            logger.log(Level.INFO, "Encoding {0}", flatBuffersSerializable);

            final var builder = new FlatBufferBuilder(1024);
            final var header = encodeHeader(flatBuffersSerializable, builder);

            final var body = encodeBodyAfterHeader(flatBuffersSerializable, builder);

            logger.log(Level.INFO, "\n| {0}B | {1} |\n| {2} |", new Object[]{header.length + body.length, Arrays.toString(header), Arrays.toString(body)});

            out
                    .writeLong(header.length + body.length)
                    .writeBytes(header)
                    .writeBytes(body);
        }
        catch (final Exception exception) {

            logger.throwing(getClass().getName(), "encode", exception);
        }
    }

    protected abstract byte [] encodeHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);

    protected abstract byte [] encodeBodyAfterHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);
}
