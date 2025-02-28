package container.game.docker.modules.examples.chats.codecs;

import container.game.docker.modules.examples.chats.models.ChatMessageFlatBufferSerializable;
import container.game.docker.ship.parents.codecs.FlatBuffersDecoder;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.List;

public final class ChatMessageFlatBuffersDecoder extends FlatBuffersDecoder<ChatMessageFlatBufferSerializable> {

    public ChatMessageFlatBuffersDecoder() {

        super(ChatMessageFlatBufferSerializable.class);
    }

    @Override
    protected ChatMessageFlatBufferSerializable decodeBodyAfterHeader(ByteBuffer in) {
        return null;
    }
}
