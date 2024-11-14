package server.game.docker.modules.chat.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public final class ChatMessageEncoder extends MessageToByteEncoder<ChatMessagePDU> {
    private static final int AUTHOR_NAME_LENGTH = 8;
    private static final int MAX_MESSAGE_LENGTH = 64;

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, ChatMessagePDU chatMessagePDU, ByteBuf out) {
//        final var authorNameByteBuffer = ByteBufUtil
//                .encodeString(
//                        Unpooled.buffer(AUTHOR_NAME_LENGTH).alloc(),
//                        CharBuffer
//                                .allocate(AUTHOR_NAME_LENGTH)
//                                .append(chatMessagePDU.getAuthorName() != null ? chatMessagePDU.getAuthorName() : "")
//                                .position(0),
//                        Charset.defaultCharset()
//                );
//        final CharBuffer chatMessageCharBuffer = CharBuffer.allocate(MAX_MESSAGE_LENGTH).append(chatMessagePDU.getMessage());
//        final ByteBuf encodedChatMessageByteBuf = ByteBufUtil.encodeString(
//                Unpooled.buffer(MAX_MESSAGE_LENGTH).alloc(),
//                chatMessageCharBuffer.position(0),
//                Charset.defaultCharset()
//        );
//
//        final ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES + AUTHOR_NAME_LENGTH + MAX_MESSAGE_LENGTH)
//                .writeByte(ChatMessagePDU.PROTOCOL_IDENTIFIER)
//                .writeLong(Long.BYTES + AUTHOR_NAME_LENGTH + MAX_MESSAGE_LENGTH)
//                .writeLong(chatMessagePDU.getAuthorID() != null ? chatMessagePDU.getAuthorID() : -1L)
//                .writeBytes(authorNameByteBuffer)
//                .writeBytes(encodedChatMessageByteBuf);
//
//        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
