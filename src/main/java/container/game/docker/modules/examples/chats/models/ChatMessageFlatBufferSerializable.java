package container.game.docker.modules.examples.chats.models;

import container.game.docker.ship.examples.compiled.schemas.ChatMessage;
import container.game.docker.ship.parents.models.FlatBufferSerializable;

/**
 * Example chat message ProtocolDataUnit.
 */
public record ChatMessageFlatBufferSerializable(String authorNick, String message) implements FlatBufferSerializable<ChatMessage> {

    @Override
    public Class<ChatMessage> getSchemaClass() {

        return ChatMessage.class;
    }
}
