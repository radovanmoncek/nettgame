package cz.radovanmoncek.ship.parents.codecs;

import com.google.flatbuffers.Table;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This class provides basic encoding utility for a given FlatBuffers {@link Table}.
 * <p>
 * Structure:
 * </p>
 * <pre>
 *     ---------------------------------------------------------
 *     |         Length 0x8    |        your header            |
 *     ---------------------------------------------------------
 *     |                        your body                      |
 *     ---------------------------------------------------------
 * </pre>
 */
public abstract class FlatBuffersDecoder<FlatBuffersSchema extends Table> extends ByteToMessageDecoder {
    private static final Logger logger = LogManager.getLogger(FlatBuffersDecoder.class);
    private static final int HEADER_SIZE = Long.BYTES;

    @Override
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf in, final List<Object> out) {

        if (in.readableBytes() < HEADER_SIZE) {

            return;
        }

        in.markReaderIndex();

        if (in.readableBytes() < in.readLong()) {

            in.resetReaderIndex();

            return;
        }

        in.markReaderIndex();

        if (in.readableBytes() == 0) {

            logger.warn("Received an empty flat buffer");

            return;
        }

        final var headerNIOBuffer = in.nioBuffer();

        if (!decodeHeader(headerNIOBuffer)) {

            channelHandlerContext.fireChannelRead(in.resetReaderIndex().retain());

            return;
        }

        in.readerIndex(in.readerIndex() + headerNIOBuffer.position());

        out.add(decodeBodyAfterHeader(in.nioBuffer()));

        in.readerIndex(in.writerIndex());
    }

    /**
     * <p>
     *     Determine whether to continue decoding this {@link ByteBuffer}, or pass it down the {@link io.netty.channel.ChannelPipeline}.
     * </p>
     * <i>Inspired by {@link io.netty.channel.SimpleChannelInboundHandler#acceptInboundMessage(Object)}.</i>
     * @param in the {@link ByteBuffer} that is being decoded.
     * @return {@code true} if this is the desired data, {@code false} otherwise.
     */
    abstract protected boolean decodeHeader(final ByteBuffer in);

    abstract protected FlatBuffersSchema decodeBodyAfterHeader(final ByteBuffer in);

    @Override
    public final void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
