package container.game.docker.ship.parents.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import container.game.docker.ship.injection.annotations.InjectEncoderBindings;
import container.game.docker.ship.parents.models.FlatBufferSerializable;
import container.game.docker.ship.parents.products.Product;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public abstract class FlatBuffersEncoder<BandAid extends FlatBufferSerializable<?>> extends MessageToByteEncoder<BandAid> implements Product {
    private static final Logger logger = LogManager.getLogger(FlatBuffersEncoder.class);
    @SuppressWarnings("unused")
    @InjectEncoderBindings
    private Map<Class<?>, Byte> flatBuffersSchemaToMagicByteBindings;

    @Override
    protected final void encode(final ChannelHandlerContext channelHandlerContext, final BandAid flatBuffersSerializable, final ByteBuf out) {

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
