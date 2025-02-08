package container.game.docker.modules.examples.lobby.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

import java.util.Collection;

import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

/**
 * Example lobby response ProtocolDataUnit.
 */
public record LobbyResponseProtocolDataUnit(LobbyFlag lobbyFlag, Integer lobbyHash, String memberNickname1, String memberNickname2) implements ProtocolDataUnit {
    private static final Byte protocolIdentifier = 4;
    private static final Byte bodyLength = 5 + 2 * MAX_NICKNAME_LENGTH;

    @Override
    public int getProtocolIdentifier() {

        return protocolIdentifier;
    }

    @Override
    public long getBodyLength() {

        return bodyLength;
    }
}
