package server.game.docker.modules.player.encoders;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import server.game.docker.modules.player.pdus.NicknameProtocolDataUnit;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

public final class NicknameEncoder extends MessageToMessageEncoder<NicknameProtocolDataUnit> {
    private static final int MAX_USERNAME_LENGTH = 8;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NicknameProtocolDataUnit nicknamePDU, List<Object> list) {
        final var usernameByteBuffer = ByteBufUtil
                .encodeString(
                        Unpooled.buffer(MAX_USERNAME_LENGTH).alloc(),
                        CharBuffer
                                .allocate(MAX_USERNAME_LENGTH)
                                .append(nicknamePDU.nickname())
                                .position(0),
                        Charset.defaultCharset()
                );

        final var byteBufOut = Unpooled.buffer(Byte.BYTES + Long.BYTES + MAX_USERNAME_LENGTH)
                .writeByte(NicknameProtocolDataUnit.PROTOCOL_IDENTIFIER)
                .writeLong(MAX_USERNAME_LENGTH)
                .writeBytes(usernameByteBuffer);

        channelHandlerContext.writeAndFlush(byteBufOut);
    }
}
