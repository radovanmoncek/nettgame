package container.game.docker.modules.chat.examples.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.chat.pdus.ChatMessageProtocolDataUnit;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public final class ChatMessageEncoder extends MessageToByteEncoder<ChatMessageProtocolDataUnit> {

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, ChatMessageProtocolDataUnit chatMessagePDU, ByteBuf out) {
        final var authorNameByteBuffer = ByteBufUtil
                .encodeString(
                        Unpooled.buffer(ChatMessageProtocolDataUnit.AUTHOR_NICK_LENGTH).alloc(),
                        CharBuffer
                                .allocate(ChatMessageProtocolDataUnit.AUTHOR_NICK_LENGTH)
                                .append(chatMessagePDU.authorNick() != null ? chatMessagePDU.authorNick() : "")
                                .position(0),
                        Charset.defaultCharset()
                );
        final CharBuffer chatMessageCharBuffer = CharBuffer.allocate(ChatMessageProtocolDataUnit.MAX_MESSAGE_LENGTH).append(chatMessagePDU.message());
        final ByteBuf encodedChatMessageByteBuf = ByteBufUtil.encodeString(
                Unpooled.buffer(ChatMessageProtocolDataUnit.MAX_MESSAGE_LENGTH).alloc(),
                chatMessageCharBuffer.position(0),
                Charset.defaultCharset()
        );

        final ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES + ChatMessageProtocolDataUnit.AUTHOR_NICK_LENGTH + ChatMessageProtocolDataUnit.MAX_MESSAGE_LENGTH)
                .writeByte(ChatMessageProtocolDataUnit.PROTOCOL_IDENTIFIER)
                .writeLong(ChatMessageProtocolDataUnit.AUTHOR_NICK_LENGTH + ChatMessageProtocolDataUnit.MAX_MESSAGE_LENGTH)
                .writeBytes(authorNameByteBuffer)
                .writeBytes(encodedChatMessageByteBuf);

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
