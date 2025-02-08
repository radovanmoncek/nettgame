package container.game.docker.ship.parents.codecs;

import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.Arrays;
import java.util.List;

/**
 * This class provides basic encoding utility.
 */
public abstract class Decoder<P extends ProtocolDataUnit> extends ByteToMessageDecoder {
    private static final int headerSize = Byte.BYTES + Long.BYTES;
    private static final byte MIN_PROTOCOL_IDENTIFIER = 0x1;
    private static final byte MAX_PROTOCOL_IDENTIFIER = 0x10;

    @Override
    protected final void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf in, final List<Object> out) {

        if (in.readableBytes() < headerSize) {

            return;
        }

        byte type;

        if ((type = in.markReaderIndex().readByte()) != supplyProtocolIdentifier()) {

            if (type < MIN_PROTOCOL_IDENTIFIER || type > MAX_PROTOCOL_IDENTIFIER) {

                throw new CorruptedFrameException(String.format("Corrupted PDU received: %s", Arrays.toString(in.array())));
            }

            channelHandlerContext.fireChannelRead(in.resetReaderIndex().retain());

            return;
        }

        if (in.readableBytes() < in.readLong()) {

            in.resetReaderIndex();

            return;
        }

        decodeBodyAfterHeader(in, out);
    }

    abstract protected void decodeBodyAfterHeader(final ByteBuf in, final List<? super P> out);

    abstract protected int supplyProtocolIdentifier();
}
