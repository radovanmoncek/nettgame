package cz.radovanmoncek.server.modules.chats.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.modules.chats.models.ChatMessageFlatBufferSerializable;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;

public final class ChatMessageFlatBuffersEncoder extends FlatBuffersEncoder<ChatMessageFlatBufferSerializable> {

    @Override
    protected byte[] encodeBodyAfterHeader(ChatMessageFlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {
        return new byte[0];
    }

    @Override
    protected byte[] encodeHeader(ChatMessageFlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {
        return new byte[0];
    }
}
