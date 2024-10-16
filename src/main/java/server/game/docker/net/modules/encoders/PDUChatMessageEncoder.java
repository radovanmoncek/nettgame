package server.game.docker.net.modules.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.pdus.PDUChatMessage;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.pdus.PDU;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class PDUChatMessageEncoder implements PDUHandlerEncoder {
    private static final int AUTHOR_NAME_LENGTH = 8;
    private static final int MAX_MESSAGE_LENGTH = 64;

    @Override
    public void encode(PDU in, Channel out) {
        PDUChatMessage chatMessage = (PDUChatMessage) in;
        ByteBuf authorNameByteBuffer = ByteBufUtil
                .encodeString(
                        Unpooled.buffer(AUTHOR_NAME_LENGTH).alloc(),
                        CharBuffer
                                .allocate(AUTHOR_NAME_LENGTH)
                                .append(chatMessage.getAuthorName() != null ? chatMessage.getAuthorName() : "")
                                .position(0),
                        Charset.defaultCharset()
                );
        CharBuffer chatMessageCharBuffer = CharBuffer.allocate(MAX_MESSAGE_LENGTH).append(chatMessage.getMessage());
        ByteBuf encodedChatMessageByteBuf = ByteBufUtil.encodeString(
                Unpooled.buffer(MAX_MESSAGE_LENGTH).alloc(),
                chatMessageCharBuffer.position(0),
                Charset.defaultCharset()
        );

        ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES + AUTHOR_NAME_LENGTH + MAX_MESSAGE_LENGTH)
                .writeByte(PDUType.CHATMESSAGE.oneBasedOrdinal())
                .writeLong(Long.BYTES + AUTHOR_NAME_LENGTH + MAX_MESSAGE_LENGTH)
                .writeLong(chatMessage.getAuthorID() != null ? chatMessage.getAuthorID() : -1L)
                .writeBytes(authorNameByteBuffer)
                .writeBytes(encodedChatMessageByteBuf);

        out.writeAndFlush(byteBuf);
    }
}
