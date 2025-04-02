package cz.radovanmoncek.ship.engine.directors;

import cz.radovanmoncek.ship.engine.builders.NettgameServerBootstrapBuilder;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Part of the Builder design pattern.
 * Used for creation of specific {@link NettgameServerBootstrapBuilder} configurations.
 */
public class NettgameServerBootstrapDirector {
    private final NettgameServerBootstrapBuilder builder;

    public NettgameServerBootstrapDirector(NettgameServerBootstrapBuilder builder) {

        this.builder = builder;
    }

    /**
     * Builds a {@link NettgameServerBootstrapBuilder} with {@link LogLevel#INFO} log level, port of 4321, address of the local machine, shutdownDelay of 4.
     * @return the configured {@link NettgameServerBootstrapBuilder}.
     */
    public NettgameServerBootstrapBuilder makeDefaultGameServerBootstrap() throws UnknownHostException {

        return builder
                .buildLogLevel(LogLevel.INFO)
                .buildPort(4321)
                .buildInternetProtocolAddress(InetAddress.getLocalHost())
                .buildShutdownDelay(4);
    }

    /**
     * Builds a {@link NettgameServerBootstrapBuilder}
     * @return the configured {@link NettgameServerBootstrapBuilder}.
     */
    public NettgameServerBootstrapBuilder makeLoopbackGameServerBootstrap() {

        return builder
                .buildLogLevel(LogLevel.INFO)
                .buildPort(4321)
                .buildInternetProtocolAddress(InetAddress.getLoopbackAddress())
                .buildShutdownDelay(4);
    }
}
