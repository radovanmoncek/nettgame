package container.game.docker.modules.player.pdus;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

public record NicknameProtocolDataUnit(String nickname) implements ProtocolDataUnit {
    public static final Byte PROTOCOL_IDENTIFIER = 1;
}
