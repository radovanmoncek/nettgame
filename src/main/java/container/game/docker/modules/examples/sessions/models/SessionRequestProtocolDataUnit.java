package container.game.docker.modules.examples.sessions.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example session ProtocolDataUnit.
 * @param sessionFlag
 * @param sessionUUID
 * @param x
 * @param y
 * @param rotationAngle
 * @param nickname
 */
public record SessionRequestProtocolDataUnit(
        SessionFlag sessionFlag,
        Integer x,
        Integer y,
        Integer rotationAngle,
        String nickname,
        String sessionUUID
        ) implements ProtocolDataUnit {

    public static SessionRequestProtocolDataUnit newSTATE(final int x, final int y, final int rotationAngle) {

        return new SessionRequestProtocolDataUnit(SessionFlag.STATE, x, y, rotationAngle, "", "");
    }

    public static SessionRequestProtocolDataUnit newSTART(final String nickname1) {

        return new SessionRequestProtocolDataUnit(SessionFlag.START, 0, 0, 0, nickname1, "");
    }

    public static SessionRequestProtocolDataUnit newJOIN(final String nickname1, final String sessionUUID) {

        return new SessionRequestProtocolDataUnit(SessionFlag.JOIN, 0, 0, 0, nickname1, sessionUUID);
    }

    public static SessionRequestProtocolDataUnit newSTOP() {

        return new SessionRequestProtocolDataUnit(SessionFlag.STOP, 0, 0, 0, "", "");
    }
}
