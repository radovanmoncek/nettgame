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

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, ChatMessagePDU chatMessagePDU, ByteBuf out) {
        final var authorNameByteBuffer = ByteBufUtil
                .encodeString(
                        Unpooled.buffer(ChatMessagePDU.AUTHOR_NICK_LENGTH).alloc(),
                        CharBuffer
                                .allocate(ChatMessagePDU.AUTHOR_NICK_LENGTH)
                                .append(chatMessagePDU.authorNick() != null ? chatMessagePDU.authorNick() : "")
                                .position(0),
                        Charset.defaultCharset()
                );
        final CharBuffer chatMessageCharBuffer = CharBuffer.allocate(ChatMessagePDU.MAX_MESSAGE_LENGTH).append(chatMessagePDU.message());
        final ByteBuf encodedChatMessageByteBuf = ByteBufUtil.encodeString(
                Unpooled.buffer(ChatMessagePDU.MAX_MESSAGE_LENGTH).alloc(),
                chatMessageCharBuffer.position(0),
                Charset.defaultCharset()
        );

        final ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES + ChatMessagePDU.AUTHOR_NICK_LENGTH + ChatMessagePDU.MAX_MESSAGE_LENGTH)
                .writeByte(ChatMessagePDU.PROTOCOL_IDENTIFIER)
                .writeLong(ChatMessagePDU.AUTHOR_NICK_LENGTH + ChatMessagePDU.MAX_MESSAGE_LENGTH)
                .writeBytes(authorNameByteBuffer)
                .writeBytes(encodedChatMessageByteBuf);

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
