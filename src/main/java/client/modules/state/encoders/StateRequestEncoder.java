package client.modules.state.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import client.modules.state.models.StateRequestProtocolDataUnit;

/**
 * Example
 */
public class StateRequestEncoder extends MessageToByteEncoder<StateRequestProtocolDataUnit> {
    @Override
    protected void encode(ChannelHandlerContext ctx, StateRequestProtocolDataUnit msg, ByteBuf out) {
        final var byteBuf = Unpooled
                .buffer(Byte.BYTES + Long.BYTES)
                .writeByte(StateRequestProtocolDataUnit.PROTOCOL_IDENTIFIER)
                .writeLong(Integer.BYTES + Integer.BYTES)
                .writeInt(msg.x())
                .writeInt(msg.y());
        ctx.writeAndFlush(byteBuf);
    }
}
