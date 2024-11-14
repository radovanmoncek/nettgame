package server.game.docker.modules.player.encoders;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import server.game.docker.modules.player.pdus.NicknamePDU;
import server.game.docker.ship.enums.PDUType;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

public final class NicknameEncoder extends MessageToMessageEncoder<NicknamePDU> {
    private static final int MAX_USERNAME_LENGTH = 8;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NicknamePDU nicknamePDU, List<Object> list) {
        final var usernameByteBuffer = ByteBufUtil
                .encodeString(
                        Unpooled.buffer(MAX_USERNAME_LENGTH).alloc(),
                        CharBuffer
                                .allocate(MAX_USERNAME_LENGTH)
                                .append(nicknamePDU.getNewNickname())
                                .position(0),
                        Charset.defaultCharset()
                );

        final var byteBufOut = Unpooled.buffer(Byte.BYTES + Long.BYTES + MAX_USERNAME_LENGTH)
                .writeByte(PDUType.USERNAME.oneBasedOrdinal())
                .writeLong(MAX_USERNAME_LENGTH)
                .writeBytes(usernameByteBuffer);

        channelHandlerContext.writeAndFlush(byteBufOut);
    }
}
