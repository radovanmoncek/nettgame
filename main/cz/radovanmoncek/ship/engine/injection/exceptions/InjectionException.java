package cz.radovanmoncek.ship.engine.injection.exceptions;

/**
 * Thrown, if a dependency injection fails, for any reason.
 */
public class InjectionException extends Exception {

    public InjectionException(final String message) {

        super(message);
    }

    public InjectionException(String string, Exception exception) {

        super(string, exception);
    }
}
