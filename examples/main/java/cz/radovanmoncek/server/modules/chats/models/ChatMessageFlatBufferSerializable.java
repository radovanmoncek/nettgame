package cz.radovanmoncek.server.modules.chats.models;

import cz.radovanmoncek.server.ship.compiled.schemas.ChatMessage;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;

/**
 * Example chat message ProtocolDataUnit.
 */
public record ChatMessageFlatBufferSerializable(String authorNick, String message) implements FlatBufferSerializable<ChatMessage> {

    @Override
    public Class<ChatMessage> getSchemaClass() {

        return ChatMessage.class;
    }
}
