package cz.radovanmoncek.ship.sessions.models;

public enum GameSessionConfigurationOption {

    /**
     * Wait a given amount of time before ending the game session with no connections.
     */
    ENABLE_TIMEOUT,
    /**
     * The maximum amount of players this game session can have.
     */
    MAX_PLAYERS
}
