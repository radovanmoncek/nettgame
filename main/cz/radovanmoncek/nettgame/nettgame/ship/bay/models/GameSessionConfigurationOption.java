package cz.radovanmoncek.nettgame.nettgame.ship.bay.models;

/**
 * Options for configuring a game session.
 *
 * @author Radovan Monček
 * @since 1.0
 */
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
    AWAIT_HOST,
    /**
     * The maximum possible length a game session can have.
     */
    MAX_LENGTH
}
