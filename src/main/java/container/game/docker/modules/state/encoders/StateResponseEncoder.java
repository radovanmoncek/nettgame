package container.game.docker.modules.state.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import container.game.docker.modules.state.models.StateResponseProtocolDataUnit;

/**
 * Example
 */
public class StateResponseEncoder extends MessageToByteEncoder<StateResponseProtocolDataUnit> {

    @Override
    protected void encode(ChannelHandlerContext ctx, StateResponseProtocolDataUnit stateResponsePDU, ByteBuf out) {
        final var byteBuf = Unpooled
                .buffer(Byte.BYTES + Long.BYTES)
                .writeByte(StateResponseProtocolDataUnit.PROTOCOL_IDENTIFIER)
                .writeLong(4 * Integer.BYTES)
                .writeInt(stateResponsePDU.x())
                .writeInt(stateResponsePDU.y())
                .writeInt(stateResponsePDU.x2())
                .writeInt(stateResponsePDU.y2());

        ctx.writeAndFlush(byteBuf);
    }
}
