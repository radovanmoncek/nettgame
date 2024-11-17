package server.game.docker.modules.state.pdus;

import server.game.docker.ship.parents.pdus.PDU;

public record StateResponsePDU(String playerNickname, Integer x, Integer y) implements PDU {
    public static final int PROTOCOL_IDENTIFIER = 10;
}
