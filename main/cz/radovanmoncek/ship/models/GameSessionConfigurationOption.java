package cz.radovanmoncek.ship.models;

public enum GameSessionConfigurationOption {

    /**
     * Wait a given amount of time before ending the game session with no connections.
     */
    ENABLE_TIMEOUT,
    /**
     * The maximum amount of players this game session can have.
     */
    MAX_PLAYERS,
    /**
     * Wait for an initial connection (the host player).
     */
    AWAIT_HOST
}
