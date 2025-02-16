package container.game.docker.modules.examples.chats.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example chat message ProtocolDataUnit.
 */
public record ChatMessageProtocolDataUnit(ChatMessageFlag chatMessageFlag, String authorNick, String message) implements ProtocolDataUnit {

    public static ChatMessageProtocolDataUnit newINVALID() {

        return new ChatMessageProtocolDataUnit(ChatMessageFlag.INVALID, null, null);
    }
}
