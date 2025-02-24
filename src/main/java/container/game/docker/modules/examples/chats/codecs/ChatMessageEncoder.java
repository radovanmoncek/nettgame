package container.game.docker.modules.examples.chats.codecs;

import container.game.docker.modules.examples.chats.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;

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
