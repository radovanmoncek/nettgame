package container.game.docker.modules.lobby.pdus;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Reliably transported PDU with 1B (Byte) + optional 8B (Long) payload.
 */
public record LobbyRequestProtocolDataUnit(Byte lobbyRequestFlag, Long leaderId) implements ProtocolDataUnit {
    public static final Byte PROTOCOL_IDENTIFIER = 3;

    public enum LobbyRequestFlag {
        CREATE,
        /**
         * Attribute {@link #leaderId} is required.
         */
        JOIN,
        LEAVE,
        INFO
    }
}
