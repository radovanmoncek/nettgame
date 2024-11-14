package server.game.docker.modules.lobby.pdus;

import server.game.docker.ship.parents.pdus.PDU;

/**
 * Reliably transported PDU with 1B (Byte) + optional 8B (Long) payload.
 */
public record LobbyRequestPDU(Byte lobbyRequestFlag, Long leaderId) implements PDU {
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
