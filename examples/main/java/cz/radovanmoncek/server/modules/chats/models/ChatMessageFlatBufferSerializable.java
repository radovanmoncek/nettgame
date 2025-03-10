package cz.radovanmoncek.server.modules.chats.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;

/**
 * Example.
 */
public record ChatMessageFlatBufferSerializable(String authorNick, String message) implements FlatBufferSerializable {

    @Override
    public byte[] serialize(FlatBufferBuilder builder) {
        return builder.sizedByteArray();
    }
}
