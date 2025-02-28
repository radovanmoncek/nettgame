package container.game.docker.ship.parents.codecs;

import container.game.docker.ship.injection.annotations.InjectDecoderBindings;
import container.game.docker.ship.parents.products.Product;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static container.game.docker.ship.parents.models.FlatBufferSerializable.*;

/**
 * This class provides basic encoding utility.
 */
public abstract class FlatBuffersDecoder<Schema> extends ByteToMessageDecoder implements Product {
    private static final Logger logger = LogManager.getLogger(FlatBuffersDecoder.class);
    private final Class<Schema> schemaClass;
    @SuppressWarnings("unused")
    @InjectDecoderBindings
    private Map<Byte, Class<?>> magicByteToFlatBuffersSchemaBindings;

    protected FlatBuffersDecoder(final Class<Schema> schemaClass) {

        this.schemaClass = schemaClass;
    }

    @Override
    protected final void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf in, final List<Object> out) {

        if (in.readableBytes() < HEADER_SIZE) {

            return;
        }

        final var magicByte = in.markReaderIndex().readByte();

        if (!magicByteToFlatBuffersSchemaBindings.get(magicByte).equals(schemaClass)) {

            channelHandlerContext.fireChannelRead(in.resetReaderIndex().retain());

            return;
        }

        if (in.readableBytes() < in.readLong()) {

            in.resetReaderIndex();

            return;
        }

        out.add(decodeBodyAfterHeader(in.nioBuffer()));
    }

    abstract protected Schema decodeBodyAfterHeader(final ByteBuffer in);

    @Override
    public final void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
