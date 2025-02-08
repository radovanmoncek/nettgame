package container.game.docker.modules.examples.chat.encoders;

import container.game.docker.modules.examples.chat.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public final class ChatMessageEncoder extends Encoder<ChatMessageProtocolDataUnit> {

    @Override
    public void encodeBodyAfterHeader(final ChatMessageProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        final var authorNicknameByteBuffer = ByteBufUtil
                .encodeString(
                        out.alloc(),
                        CharBuffer.wrap(protocolDataUnit.authorNick() != null ? protocolDataUnit.authorNick() : ""),
                        Charset.defaultCharset()
                );
        final var encodedChatMessageByteBuf = ByteBufUtil
                .encodeString(
                        out.alloc(),
                        CharBuffer.wrap(protocolDataUnit.message()),
                        Charset.defaultCharset()
                );

        out
                .writeBytes(authorNicknameByteBuffer)
                .writeBytes(encodedChatMessageByteBuf);
    }
}
