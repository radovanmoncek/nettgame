package server.game.docker.modules.session.pdus;

import server.game.docker.ship.parents.pdus.PDU;

public record SessionPDU(Byte sessionFlag) implements PDU {
    public static final Byte PROTOCOL_IDENTIFIER = 7;

    public enum SessionFlag {
        START, JOIN, STOP
    }
}
