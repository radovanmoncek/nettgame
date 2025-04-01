package cz.radovanmoncek.ship.parents.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
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

        logger.log(Level.INFO, "Encoding {0}", flatBuffersSerializable);

        final var builder = new FlatBufferBuilder(1024);
        final var header = encodeHeader(flatBuffersSerializable, builder);
        final var body = encodeBodyAfterHeader(flatBuffersSerializable, builder);

        out
                .writeLong(header.length + body.length)
                .writeBytes(header)
                .writeBytes(body);

        renderEncodedData(new byte[]{(byte) (header.length + body.length)}, header, body);
    }

    protected abstract byte[] encodeHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);

    protected abstract byte[] encodeBodyAfterHeader(final BandAid flatBuffersSerializable, final FlatBufferBuilder flatBufferBuilder);

    private void renderEncodedData(byte[] length, byte[] header, byte[] body) {

        if (header.length == 0 || body.length == 0) {

            return;
        }

        final var renderingResult = new StringBuilder();

        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < body.length + 3; j++) {

                /*if (j == 0) {

                    renderingResult.append("|");

                    continue;
                }*/

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

                        renderingResult.append(length[0]);

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
                                .append(body[j])
                                .append(" ");

                        continue;
                    }
                }
                /*if (j == 1 || j == (body.length + 3) - 2) {

                    renderingResult.append(" ");

                    continue;
                }*/

                /*if (i == 1 && j < body.length) {

                    renderingResult.append(body[j]);
                }

                if (i == 0) {

                    renderingResult.append(length[0]);
                    renderingResult.append(" | ");
                    renderingResult.append(header[0]);
                }*/

                renderingResult.append(" ");
            }
        }

        logger.log(Level.INFO, "\n {0}", renderingResult);
    }
}
