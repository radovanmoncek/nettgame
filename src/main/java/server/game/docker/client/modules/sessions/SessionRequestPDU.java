package server.game.docker.client.modules.sessions;

import server.game.docker.ship.parents.pdus.PDU;

public record SessionRequestPDU(Long sessionLeaderId) implements PDU {
    public static final Byte protocolIdentifier = 7;
    public enum SessionRequestFlag {
        START, JOIN, STOP
    }
}
