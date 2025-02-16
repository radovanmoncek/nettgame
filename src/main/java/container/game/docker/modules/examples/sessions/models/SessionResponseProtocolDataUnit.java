package container.game.docker.modules.examples.sessions.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

public record SessionResponseProtocolDataUnit(
        SessionFlag sessionFlag,
        Integer x1,
        Integer y1,
        Integer rotationAngle1,
        Integer x2,
        Integer y2,
        Integer rotationAngle2,
        String nickname1,
        String nickname2,
        String sessionUUID
) implements ProtocolDataUnit {

    public static SessionResponseProtocolDataUnit newSTATE(
            final Integer x1,
            final Integer y1,
            final Integer rotationAngle1,
            final Integer x2,
            final Integer y2,
            final Integer rotationAngle2
    ) {

        return new SessionResponseProtocolDataUnit(
                SessionFlag.STATE,
                x1,
                y1,
                rotationAngle1,
                x2,
                y2,
                rotationAngle2,
                "",
                "",
                ""
        );
    }

    public static SessionResponseProtocolDataUnit newSTART(final String nickname1, final String sessionUUID) {

        return new SessionResponseProtocolDataUnit(
                SessionFlag.START,
                0,
                0,
                0,
                0,
                0,
                0,
                nickname1,
                "",
                sessionUUID
        );
    }

    public static SessionResponseProtocolDataUnit newSTOP() {

        return new SessionResponseProtocolDataUnit(
                SessionFlag.STOP,
                0,
                0,
                0,
                0,
                0,
                0,
                "",
                "",
                ""
        );
    }

    public static SessionResponseProtocolDataUnit newINVALID() {

        return new SessionResponseProtocolDataUnit(
                SessionFlag.INVALID,
                0,
                0,
                0,
                0,
                0,
                0,
                "",
                "",
                ""
        );
    }

    public static SessionResponseProtocolDataUnit newJOIN(final String nickname1, final String nickname2) {

        return new SessionResponseProtocolDataUnit(
                SessionFlag.JOIN,
                0,
                0,
                0,
                0,
                0,
                0,
                nickname1,
                nickname2,
                ""
        );
    }
}
