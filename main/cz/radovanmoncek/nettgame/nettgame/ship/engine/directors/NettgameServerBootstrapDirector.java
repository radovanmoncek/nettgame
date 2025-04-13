package cz.radovanmoncek.nettgame.nettgame.ship.engine.directors;

import cz.radovanmoncek.nettgame.nettgame.ship.engine.builders.NettgameServerBootstrapBuilder;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Part of the Builder design pattern.
 * Used for creation of specific {@link NettgameServerBootstrapBuilder} configurations.
 *
 * @author Radovan Monček
 * @since 1.0
 */
public class NettgameServerBootstrapDirector {
    private final NettgameServerBootstrapBuilder builder;

    public NettgameServerBootstrapDirector(NettgameServerBootstrapBuilder builder) {

        this.builder = builder;
    }

    /**
     * Builds a {@link NettgameServerBootstrapBuilder} configured as such:
     * <ul>
     * <li>{@link LogLevel#INFO} log level,</li>
     * <li>port of 4321,</li>
     * <li>address of the local machine, </li>
     * <li>and shutdownDelay of 4.</li>
     * </ul>
     *
     * @return the configured {@link NettgameServerBootstrapBuilder}.
     * @author Radovan Monček
     * @see #makeLoopbackGameServerBootstrap()
     * @since 1.0
     * @apiNote § this method voids the "methods must not produce exceptions as their terminating output" rule,
     * but an exception can be made, as it the existence of a running game server without configuration should be impossible.
     * @throws UnknownHostException if there is an issue with resolving the host name of this machine.
     */
    public NettgameServerBootstrapBuilder makeDefaultGameServerBootstrap() throws UnknownHostException {

        return builder
                .buildLogLevel(LogLevel.INFO)
                .buildPort(4321)
                .buildInternetProtocolAddress(InetAddress.getLocalHost())
                .buildShutdownDelay(4);
    }

    /**
     * Builds a {@link NettgameServerBootstrapBuilder} loopback configuration.
     *
     * @return the configured {@link NettgameServerBootstrapBuilder}.
     * @see #makeDefaultGameServerBootstrap()
     * @author Radovan Monček
     * @since 1.0
     */
    public NettgameServerBootstrapBuilder makeLoopbackGameServerBootstrap() {

        return builder
                .buildLogLevel(LogLevel.INFO)
                .buildPort(4321)
                .buildInternetProtocolAddress(InetAddress.getLoopbackAddress())
                .buildShutdownDelay(4);
    }
}
