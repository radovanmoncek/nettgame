package container.game.docker.modules.session.models;

public record SessionProtocolDataUnit(Byte sessionFlag) implements container.game.docker.ship.parents.models.ProtocolDataUnit {
    public static final Byte PROTOCOL_IDENTIFIER = 7;

    public enum SessionFlag {
        START, STOP
    }
}
