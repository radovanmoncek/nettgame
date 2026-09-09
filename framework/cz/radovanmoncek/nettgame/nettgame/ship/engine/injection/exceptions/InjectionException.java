package cz.radovanmoncek.nettgame.nettgame.ship.engine.injection.exceptions;

/**
 * Thrown, if a dependency injection fails, for any reason.
 * @since 1.0
 * @author Radovan Monček
 */
public class InjectionException extends Exception {

    public InjectionException(final String message) {

        super(message);
    }

    public InjectionException(String string, Exception exception) {

        super(string, exception);
    }
}
