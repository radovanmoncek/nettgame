package container.game.docker.ship.parents.codecs;

import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;

public abstract class Encoder<P extends ProtocolDataUnit> extends MessageToByteEncoder<P> {
    private final Map<Class<? extends ProtocolDataUnit>, Byte> protocolDataUnitToProtocolIdentifierBindings;

    public Encoder(final Map<Class<? extends ProtocolDataUnit>, Byte> protocolDataUnitToProtocolIdentifierBindings) {

        this.protocolDataUnitToProtocolIdentifierBindings = protocolDataUnitToProtocolIdentifierBindings;
    }

    @Override
    protected final void encode(final ChannelHandlerContext channelHandlerContext, final P protocolDataUnit, final ByteBuf out) {

        out.writeByte(protocolDataUnitToProtocolIdentifierBindings.get(protocolDataUnit.getClass()));

        final var bodyByteBuf = channelHandlerContext.alloc().buffer();

        encodeBodyAfterHeader(protocolDataUnit, bodyByteBuf);

        final var bodyLength = bodyByteBuf.writerIndex();

        out
                .writeLong(bodyLength)
                .writeBytes(bodyByteBuf);

        bodyByteBuf.release();
    }

    protected abstract void encodeBodyAfterHeader(final P protocolDataUnit, final ByteBuf out);

    public final ByteBuf encodeString(final String in, final ByteBuf out) {

        out
                .writeInt(in.length())
                .writeCharSequence(CharBuffer.allocate(in.length()).append(in).position(0), Charset.defaultCharset());

        return out;
    }
}
