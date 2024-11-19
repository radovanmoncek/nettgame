package server.game.docker.modules.chat.pdus;

import server.game.docker.ship.parents.pdus.PDU;

/**
 * <p>
 *     Reliably transported non-empty variable length PDU (char array max size 64)
 * </p>
 * PDU:
 * <pre>
 *     --------------------------------------
 *     | AuthorNick(8B)  |   Message(64B)   |
 *     --------------------------------------
 * </pre>
 */
public record ChatMessagePDU(String authorNick, String message) implements PDU {
    public static final int PROTOCOL_IDENTIFIER = 6;
    public static final int AUTHOR_NICK_LENGTH = 8;
    public static final int MAX_MESSAGE_LENGTH = 64;
}
