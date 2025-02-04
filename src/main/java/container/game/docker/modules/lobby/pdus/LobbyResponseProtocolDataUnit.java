package container.game.docker.modules.lobby.pdus;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

import java.util.Collection;

/**
 * <p>
 * Reliably transmitted variable length {@link ProtocolDataUnit} with a length of 10B + 4 * 8B (Long)
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
public record LobbyResponseProtocolDataUnit(Byte lobbyUpdateResponseFlag, Long leaderId, Collection<String> members) implements ProtocolDataUnit {
    public static final Byte PROTOCOL_IDENTIFIER = 4;

    public enum LobbyUpdateResponseFlag {
        CREATED, JOINED, LEFT, MEMBERJOINED, MEMBERLEFT
    }
}
