package container.game.docker.modules.examples.chat.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

import static container.game.docker.modules.examples.chat.handlers.ChatChannelGroupHandler.MAX_MESSAGE_LENGTH;
import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

/**
 * <p>
 *     Reliably transported non-empty variable length PDU (char array max size 64)
 * </p>
 * PDU:
 * <pre>
 *     --------------------------------------+
 *     | AuthorNick(8B)  |   Message(64B)   |
 *     --------------------------------------
 * </pre>
 */
public record ChatMessageProtocolDataUnit(String authorNick, String message) implements ProtocolDataUnit {
    private static final int protocolIdentifier = 6;
    private static final long bodyLength = MAX_NICKNAME_LENGTH + MAX_MESSAGE_LENGTH;

    @Override
    public int getProtocolIdentifier() {
        return protocolIdentifier;
    }

    @Override
    public long getBodyLength() {
        return bodyLength;
    }
}
