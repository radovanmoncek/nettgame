package container.game.docker.ship.parents.codecs;

import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public abstract class Encoder<P extends ProtocolDataUnit> extends MessageToByteEncoder<P> {

    @Override
    protected final void encode(final ChannelHandlerContext channelHandlerContext, final P protocolDataUnit, final ByteBuf out) {

        encodeBodyAfterHeader(
                protocolDataUnit,
                out
                        .writeByte(protocolDataUnit.getProtocolIdentifier())
                        .writeLong(protocolDataUnit.getBodyLength())
        );

        channelHandlerContext.writeAndFlush(out);
    }

    abstract protected void encodeBodyAfterHeader(final P protocolDataUnit, final ByteBuf out);
}
