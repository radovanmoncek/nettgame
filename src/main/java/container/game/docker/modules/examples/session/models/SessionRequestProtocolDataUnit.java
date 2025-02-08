package container.game.docker.modules.examples.session.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

/**
 * Example session ProtocolDataUnit.
 * @param sessionFlag
 * @param sessionHash
 * @param x
 * @param y
 * @param rotationAngle
 * @param nickname
 */
public record SessionRequestProtocolDataUnit(SessionFlag sessionFlag, Integer sessionHash, Integer x, Integer y, Integer rotationAngle, String nickname) implements ProtocolDataUnit {
    private static final Byte protocolIdentifier = 7;
    private static final long bodyLength = 25 + MAX_NICKNAME_LENGTH;

    @Override
    public int getProtocolIdentifier() {

        return protocolIdentifier;
    }

    @Override
    public long getBodyLength() {

        return bodyLength;
    }
}
