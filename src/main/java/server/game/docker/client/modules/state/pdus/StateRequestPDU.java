package server.game.docker.client.modules.state.pdus;

import server.game.docker.ship.parents.pdus.PDU;

/**
 * Example state request PDU
 */
public record StateRequestPDU(Integer x, Integer y) implements PDU {
    public static final int PROTOCOL_IDENTIFIER = 9;
}