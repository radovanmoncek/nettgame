package container.game.docker.modules.examples.session.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

public record SessionResponseProtocolDataUnit(
        SessionFlag sessionFlag,
        Integer sessionHash,
        Integer x1,
        Integer y1,
        Integer rotationAngle1,
        Integer x2,
        Integer y2,
        Integer rotationAngle2,
        String nickname1,
        String nickname2
) implements ProtocolDataUnit {
    private static final Byte protocolIdentifier = 10;
    private static final long bodyLength = 1 + 30 + 2 * MAX_NICKNAME_LENGTH;

    @Override
    public int getProtocolIdentifier() {

        return protocolIdentifier;
    }

    @Override
    public long getBodyLength() {

        return bodyLength;
    }
}
