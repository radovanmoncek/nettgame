package container.game.docker.modules.examples.lobbies.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example lobby response ProtocolDataUnit.
 */
public record LobbyResponseProtocolDataUnit(
        LobbyFlag lobbyFlag,
        String memberNickname1,
        String memberNickname2,
        String lobbyUUID
) implements ProtocolDataUnit {

    public static LobbyResponseProtocolDataUnit newINVALID() {

        return new LobbyResponseProtocolDataUnit(LobbyFlag.INVALID, null, null, null);
    }
}
