package container.game.docker.modules.examples.lobby.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example lobby response ProtocolDataUnit.
 */
public record LobbyResponseProtocolDataUnit(
        LobbyFlag lobbyFlag,
        String memberNickname1,
        String memberNickname2,
        String lobbyUUID
) implements ProtocolDataUnit {}
