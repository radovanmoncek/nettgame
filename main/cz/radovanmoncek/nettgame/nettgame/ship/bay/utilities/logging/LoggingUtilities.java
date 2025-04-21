package cz.radovanmoncek.nettgame.nettgame.ship.bay.utilities.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for logging.
 * @since 1.0
 * @author Radovan Monček
 */
public final class LoggingUtilities {

    private LoggingUtilities() {
    }

    public static void enableGlobalLoggingLevel(Level level) {

        Logger.getLogger("").setLevel(level);

        for (final var handler : Logger.getLogger("").getHandlers()) {

            handler.setLevel(level);
        }
    }
}
