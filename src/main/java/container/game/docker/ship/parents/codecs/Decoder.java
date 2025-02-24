package container.game.docker.ship.parents.codecs;

import container.game.docker.ship.parents.models.ProtocolDataUnit;
import container.game.docker.ship.parents.products.Product;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static container.game.docker.ship.parents.models.ProtocolDataUnit.*;

/**
 * This class provides basic encoding utility.
 */
public abstract class Decoder<P extends ProtocolDataUnit> extends ByteToMessageDecoder implements Product {
    private static final Logger logger = LogManager.getLogger(Decoder.class);
    private final Class<P> protocolDataUnitClass;
    private final Map<Byte, Class<? extends ProtocolDataUnit>> protocolIdentifierToProtocolDataUnitBindings;

    public Decoder(final Class<P> protocolDataUnitClass) {

        this.protocolIdentifierToProtocolDataUnitBindings = new HashMap<>();
        this.protocolDataUnitClass = protocolDataUnitClass;
    }

    @Override
    protected final void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf in, final List<Object> out) {

        if (in.readableBytes() < HEADER_SIZE) {

            return;
        }

        final var protocolIdentifier = in.markReaderIndex().readByte();

        if (!protocolIdentifierToProtocolDataUnitBindings.get(protocolIdentifier).equals(protocolDataUnitClass)) {

            if (protocolIdentifier < MIN_PROTOCOL_IDENTIFIER || protocolIdentifier > MAX_PROTOCOL_IDENTIFIER) {

                throw new CorruptedFrameException("Protocol identifier " + protocolIdentifier + " out of range");
            }

            channelHandlerContext.fireChannelRead(in.resetReaderIndex().retain());

            return;
        }

        if (in.readableBytes() < in.readLong()) {

            in.resetReaderIndex();

            return;
        }

        decodeBodyAfterHeader(in, out);

        in.readerIndex(in.writerIndex());
    }

    abstract protected void decodeBodyAfterHeader(final ByteBuf in, final List<? super P> out);

    @Override
    public final void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }

    public final String decodeString(final ByteBuf in) {

        return in.readCharSequence(in.readInt(), Charset.defaultCharset()).toString().trim();
    }
}
