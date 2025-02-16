package container.game.docker.modules.examples.chat.codecs;

import container.game.docker.modules.examples.chat.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public final class ChatMessageDecoder extends Decoder<ChatMessageProtocolDataUnit> {

    public ChatMessageDecoder(final Map<Byte, Class<? extends ProtocolDataUnit>> protocolIdentifierToProtocolDataUnitBindings) {

        super(protocolIdentifierToProtocolDataUnitBindings, ChatMessageProtocolDataUnit.class);
    }

    @Override
    public void decodeBodyAfterHeader(final ByteBuf in, final List<? super ChatMessageProtocolDataUnit> out) {

        out.add(
                new ChatMessageProtocolDataUnit(
                        decodeString(in),
                        decodeString(in)
                )
        );
    }
}
