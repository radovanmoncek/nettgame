package client.modules.state.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example state request PDU
 */
public record StateRequestProtocolDataUnit(Integer x, Integer y) implements ProtocolDataUnit {
    public static final int PROTOCOL_IDENTIFIER = 9;
}