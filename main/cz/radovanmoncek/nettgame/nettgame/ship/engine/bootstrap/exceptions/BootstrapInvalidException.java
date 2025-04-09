package cz.radovanmoncek.nettgame.nettgame.ship.engine.bootstrap.exceptions;

/**
 * If this checked exception is thrown, something went wrong with the initialization process of nettgame,
 * and it could not continue its proper operation.
 *
 * @author Radovan Monček
 * @since 1.0
 */
public class BootstrapInvalidException extends Exception {

    /**
     * Constructs a new instance.
     * @param message the reason for the exception to occur.
     */
    public BootstrapInvalidException(final String message) {

        super(message);
    }
}
