package server.game.docker.modules.state.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.state.pdus.StateResponsePDU;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Example
 */
public class StateResponseEncoder extends MessageToByteEncoder<StateResponsePDU> {

    @Override
    protected void encode(ChannelHandlerContext ctx, StateResponsePDU stateResponsePDU, ByteBuf out) {
        final var byteBuf = Unpooled
                .buffer(Byte.BYTES + Long.BYTES)
                .writeByte(StateResponsePDU.PROTOCOL_IDENTIFIER)
                .writeLong(4 * Integer.BYTES)
                .writeInt(stateResponsePDU.x())
                .writeInt(stateResponsePDU.y())
                .writeInt(stateResponsePDU.x2())
                .writeInt(stateResponsePDU.y2());

        ctx.writeAndFlush(byteBuf);
    }
}
