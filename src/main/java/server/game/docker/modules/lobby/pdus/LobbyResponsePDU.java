package server.game.docker.modules.lobby.pdus;

import server.game.docker.ship.parents.pdus.PDU;

import java.util.Collection;

/**
 * <p>
 * Reliably transmitted variable length {@link PDU} with a length of 10B + 4 * 8B (Long)
 * </p>
 * PDU
 * <pre>
 *     --------------------------------
 *     | StateFlag(1B) | LeaderId(8B) |
 *     --------------------------------
 *     |        Members(max 4*8B)     |
 *     --------------------------------
 * </pre>
 */
public record LobbyResponsePDU(Byte lobbyUpdateResponseFlag, Long leaderId, Collection<String> members) implements PDU {
    public static final Byte PROTOCOL_IDENTIFIER = 4;

    public enum LobbyUpdateResponseFlag {
        CREATED, JOINED, LEFT, MEMBERJOINED, MEMBERLEFT
    }
}
