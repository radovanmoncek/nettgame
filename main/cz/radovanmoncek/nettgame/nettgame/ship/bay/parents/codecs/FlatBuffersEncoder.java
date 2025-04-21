package cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.nettgame.nettgame.ship.deck.models.FlatBufferSerializable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Netty encoder that is able to work with FlatBuffers serialized data.
 *
 * @param <BandAid> a class that is able to produce FlatBuffers encoded data of its attributes;
 *                  more formally: any class, that is an implementor of the {@link FlatBufferSerializable} interface.
 * @author Radovan Monček
 * @implNote <pre>
 *  The encoded data will be structured as such:
 *  +-----------------------+-------------------------------+
 *  |         Length 0x8    |        your header            |
 *  +-----------------------+-------------------------------+
 *  |                        your body                      |
 *  +-------------------------------------------------------+
 * </pre>
 * @since 1.0
 */
public abstract class FlatBuffersEncoder<BandAid extends FlatBufferSerializable> extends MessageToByteEncoder<BandAid> {
    private static final Logger logger = Logger.getLogger(FlatBuffersEncoder.class.getName());

    @Override
    protected void encode(final ChannelHandlerContext channelHandlerContext, final BandAid flatBuffersSerializable, final ByteBuf out) {

        // todo: encoding FlatBuffersSerializable.toString() ???? ????

        final var builder = new FlatBufferBuilder(1024);
        final var header = encodeHeader(flatBuffersSerializable, builder);
        final var body = encodeBodyAfterHeader(flatBuffersSerializable, builder);

        out
                .writeLong(header.length + body.length)
                .writeBytes(header)
                .writeBytes(body);

        renderEncodedData(header.length + body.length, header, body);
    }

    /**
     * Specify, how the header message will be encoded.
     * @param flatBuffersSerializable the message.
     * @param flatBufferBuilder convenience instance of FLatBuffersBuilder.
     * @return the binary format of the encoded message header.
     * @since 1.0
     * @author Radovan Monček
     */
    protected abstract byte[] encodeHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);

    /**
     * Specify, how the message will be ancoded.
     * @param flatBuffersSerializable the message
     * @param flatBufferBuilder convenience instance of FlatBuffersBuilder.
     * @return the binary format of the encoded message.
     * @author Radovan Monček
     * @since 1.0
     */
    protected abstract byte[] encodeBodyAfterHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);

    private void renderEncodedData(int length, byte[] header, byte[] body) {

        if (header.length == 0 || body.length == 0) {

            return;
        }

        final var renderingResult = new StringBuilder();
        var skipIterations = 0;

        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < body.length + 3; j++) {

                if (skipIterations > 0) {

                    --skipIterations;

                    continue;
                }

                if (i == 0 || i == 2 || i == 4) {

                    if(j == 0) {

                        renderingResult.append("+");

                        continue;
                    }

                    if (j == (body.length + 3) - 1) {

                        renderingResult.append("+\n ");

                        continue;
                    }

                    renderingResult.append("-");

                    continue;
                }

                else {

                    if ((i == 1 && j == (((body.length + 3) - 1) * 3 / 4))) {

                        renderingResult.append(header[0]);

                        continue;
                    }

                    if ((i == 1 && j == ((body.length + 3) - 1) / 4)) {

                        renderingResult.append(length);

                        skipIterations += Long.numberOfTrailingZeros(length);

                        continue;
                    }

                    if (j == 0 || (i == 1 && j == ((body.length + 3) - 1) / 2)){

                        renderingResult.append("|");

                        continue;
                    }

                    if (j == (body.length + 3) - 1) {

                        renderingResult.append("|\n ");

                        continue;
                    }

                    if (i == 3 && j < body.length) {

                        renderingResult
                                .append(" ")
                                .append(body[j]);

                        continue;
                    }
                }

                renderingResult.append(" ");
            }
        }

        logger.log(Level.INFO, "\n {0}", renderingResult);
    }
}
