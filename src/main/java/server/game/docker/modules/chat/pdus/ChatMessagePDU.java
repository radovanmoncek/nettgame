package server.game.docker.modules.chat.pdus;

import server.game.docker.ship.parents.pdus.PDU;

/**
 * <p>
 *     Reliably transported non-empty variable length PDU (char array max size 64)
 * </p>
 * PDU:
 * <pre>
 *     --------------------------------
 *     | AuthorID(8B) |   Msg(64B)    |
 *     --------------------------------
 * </pre>
 */
public record ChatMessagePDU(String message) implements PDU {
    public static final int PROTOCOL_IDENTIFIER = 6;
}
