package cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.codecs;

import com.google.flatbuffers.Table;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

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
 * Inspired by: <a href=https://github.com/netty/netty/blob/4.1/example/src/main/java/io/netty/example/factorial/BigIntegerDecoder.java>this</a>
 */
public abstract class FlatBuffersDecoder<FlatBuffersSchema extends Table> extends ByteToMessageDecoder {
    private static final Logger logger = Logger.getLogger(FlatBuffersDecoder.class.getName());
    private static final int lengthFieldSize = Long.BYTES;

    @Override
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf in, final List<Object> out) {

        if (in.readableBytes() < lengthFieldSize) {

            return;
        }

        in.markReaderIndex();

        final var length = in.readLong();
        final var headerLength = 1;
        final var bodyLength = (int) length - headerLength;

        if (in.readableBytes() < length) {

            in.resetReaderIndex();

            return;
        }

        if (in.readableBytes() == 0) {

            logger.warning("Received an empty flat buffer");

            return;
        }

        final var header = in.readBytes(headerLength).nioBuffer();

        if (!decodeHeader(header)) {

            final var mark = new AtomicBoolean(false);
            final var retainAndSend = new AtomicBoolean(false);

            channelHandlerContext
                    .pipeline()
                    .forEach(entry -> {

                if (retainAndSend.get() || (!mark.get() && !(entry.getValue().equals(this)))) {

                    return;
                }

                mark.set(true);

                if (!entry.getValue().equals(this) && entry.getValue() instanceof FlatBuffersDecoder<?>)
                    retainAndSend.set(true);
            });

            if (retainAndSend.get())
                channelHandlerContext.fireChannelRead(in.resetReaderIndex().retain());

            return;
        }

        final var body = in.readBytes(bodyLength).nioBuffer();
        final var decodedSchema = decodeBodyAfterHeader(body);

        out.add(decodedSchema);
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

    /**
     * Here, the actual ByteBuffers schema decoding takes place.
     * @param in the received bytes.
     * @return the decoded FLatBuffers schema.
     */
    abstract protected FlatBuffersSchema decodeBodyAfterHeader(final ByteBuffer in);

    @Override
    public final void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.throwing(getClass().getName(), "exceptionCaught", cause);
    }
}
