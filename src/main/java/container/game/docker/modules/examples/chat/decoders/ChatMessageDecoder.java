package container.game.docker.modules.examples.chat.decoders;

import container.game.docker.modules.examples.chat.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;
import java.util.List;

import static container.game.docker.modules.examples.chat.handlers.ChatChannelGroupHandler.MAX_MESSAGE_LENGTH;
import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

public final class ChatMessageDecoder extends Decoder<ChatMessageProtocolDataUnit> {

    @Override
    public void decodeBodyAfterHeader(final ByteBuf in, final List<? super ChatMessageProtocolDataUnit> out) {

        out.add(
                new ChatMessageProtocolDataUnit(
                        in
                                .toString(
                                        in.readerIndex(),
                                        MAX_NICKNAME_LENGTH,
                                        Charset.defaultCharset()
                                )
                                .trim(),
                        in
                                .toString(
                                        in.readerIndex(MAX_NICKNAME_LENGTH).readerIndex(),
                                        MAX_MESSAGE_LENGTH,
                                        Charset.defaultCharset()
                                )
                                .trim()
                )
        );
    }

    @Override
    protected int supplyProtocolIdentifier() {

        return new ChatMessageProtocolDataUnit(null, null)
                .getProtocolIdentifier();
    }
}
