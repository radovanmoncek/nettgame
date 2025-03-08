package cz.radovanmoncek.ship.parents.codecs;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.injection.annotations.InjectDecoderBindings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static cz.radovanmoncek.ship.parents.models.FlatBufferSerializable.*;

/**
 * This class provides basic encoding utility for a given FlatBuffers {@link Table}.
 */
public abstract class FlatBuffersDecoder<Schema extends Table> extends ByteToMessageDecoder {
    private static final Logger logger = LogManager.getLogger(FlatBuffersDecoder.class);
    private static final int HEADER_SIZE = Byte.BYTES + Long.BYTES;
    private final Class<Schema> schemaClass;
    @SuppressWarnings("unused")
    @InjectDecoderBindings
    private Map<Byte, Class<? extends Table>> magicByteToFlatBuffersSchemaBindings;

    protected FlatBuffersDecoder(final Class<Schema> schemaClass) {

        this.schemaClass = schemaClass;
    }

    @Override
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf in, final List<Object> out) {

        if (in.readableBytes() < HEADER_SIZE) {

            return;
        }

        final var magicByte = in
                .markReaderIndex()
                .readByte();

        if (!magicByteToFlatBuffersSchemaBindings.getOrDefault(magicByte, Table.class).equals(schemaClass)) {

            channelHandlerContext.fireChannelRead(in.resetReaderIndex().retain());

            return;
        }

        if (in.readableBytes() < in.readLong()) {

            in.resetReaderIndex();

            return;
        }

        if (in.readableBytes() == 0) {

            logger.warn("Received an empty flat buffer");

            return;
        }

        out.add(decodeBodyAfterHeader(in.nioBuffer()));

        in.readerIndex(in.writerIndex());
    }

    abstract protected Schema decodeBodyAfterHeader(final ByteBuffer in);

    @Override
    public final void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
