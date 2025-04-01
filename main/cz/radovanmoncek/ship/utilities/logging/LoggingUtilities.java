package cz.radovanmoncek.ship.utilities.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingUtilities {

    private LoggingUtilities() {}

    public static void enableGlobalLoggingLevel(Level level) {

        Logger.getLogger("").setLevel(level);

        for(final var handler : Logger.getLogger("").getHandlers()) {

            handler.setLevel(level);
        }
    }
}
