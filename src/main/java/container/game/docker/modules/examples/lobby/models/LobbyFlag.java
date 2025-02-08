package container.game.docker.modules.examples.lobby.models;

public enum LobbyFlag {

    CREATE,
    /**
     * Attribute {@link LobbyRequestProtocolDataUnit#lobbyHash()} is required.
     */
    JOIN,
    LEAVE,
    INFO,
    MEMBER_JOINED,
    MEMBER_LEFT
}
