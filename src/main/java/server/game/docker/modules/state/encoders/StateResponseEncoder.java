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
    private static final int MAXIMUM_PLAYER_NICKNAME_LENGTH = 8;

    @Override
    protected void encode(ChannelHandlerContext ctx, StateResponsePDU msg, ByteBuf out) {
        final var byteBuf = Unpooled
                .buffer(Byte.BYTES + Long.BYTES)
                .writeByte(StateResponsePDU.PROTOCOL_IDENTIFIER)
                .writeLong(MAXIMUM_PLAYER_NICKNAME_LENGTH + 2 * Integer.BYTES)
                .writeBytes(
                        ByteBufUtil.encodeString(
                                Unpooled.buffer(MAXIMUM_PLAYER_NICKNAME_LENGTH).alloc(),
                                CharBuffer.allocate(MAXIMUM_PLAYER_NICKNAME_LENGTH).append(msg.playerNickname()).position(0),
                                Charset.defaultCharset()
                        )
                )
                .writeInt(msg.x())
                .writeInt(msg.y());
        ctx.writeAndFlush(byteBuf);
    }
}
