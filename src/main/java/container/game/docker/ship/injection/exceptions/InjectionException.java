package container.game.docker.ship.injection.exceptions;

public class InjectionException extends Exception {

    public InjectionException(final String message) {

        super(message);
    }

    public InjectionException(String string, Exception exception) {

        super(string, exception);
    }
}
