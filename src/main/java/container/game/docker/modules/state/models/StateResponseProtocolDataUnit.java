package container.game.docker.modules.state.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

public record StateResponseProtocolDataUnit(Integer x, Integer y, Integer rotationAngle, Integer x2, Integer y2) implements ProtocolDataUnit {
    public static final int PROTOCOL_IDENTIFIER = 10;
}
