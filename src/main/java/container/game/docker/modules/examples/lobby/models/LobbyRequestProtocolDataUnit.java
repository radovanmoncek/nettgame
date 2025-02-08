package container.game.docker.modules.examples.lobby.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example lobby ProtocolDataUnit.
 */
public record LobbyRequestProtocolDataUnit(LobbyFlag lobbyFlag, Integer lobbyHash) implements ProtocolDataUnit {
    private static final Byte protocolIdentifier = 3;
    private static final long bodyLength = Byte.BYTES + Integer.BYTES;

    @Override
    public int getProtocolIdentifier() {

        return protocolIdentifier;
    }

    @Override
    public long getBodyLength() {

        return bodyLength;
    }
}
