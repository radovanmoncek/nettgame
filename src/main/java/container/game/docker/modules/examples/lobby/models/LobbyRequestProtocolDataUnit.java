package container.game.docker.modules.examples.lobby.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example lobby ProtocolDataUnit.
 */
public record LobbyRequestProtocolDataUnit(LobbyFlag lobbyFlag, String lobbyUUID) implements ProtocolDataUnit {}
