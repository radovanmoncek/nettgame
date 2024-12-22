package server.game.docker.modules.state.pdus;

import server.game.docker.ship.parents.pdus.PDU;

public record StateResponsePDU(Integer x, Integer y, Integer rotationAngle, Integer x2, Integer y2) implements PDU {
    public static final int PROTOCOL_IDENTIFIER = 10;
}
