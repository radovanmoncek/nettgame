package cz.radovanmoncek.ship.directors;

import cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder;
import cz.radovanmoncek.ship.creators.MySQLRepositoryCreator;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class GameServerBootstrapDirector {
    private final GameServerBootstrapBuilder builder;

    public GameServerBootstrapDirector(GameServerBootstrapBuilder builder) {
        this.builder = builder;
    }

    /**
     * Builds a {@link GameServerBootstrapBuilder} with {@link LogLevel#INFO} log level, port of 4321, address of localhost, shutdownDelay of 4, and MySQL database support.
     * @return the configured {@link GameServerBootstrapBuilder}.
     */
    public GameServerBootstrapBuilder makeDefaultGameServerBootstrap() {

        return builder
                .buildLogLevel(LogLevel.INFO)
                .buildPort(4321)
                .buildInternetProtocolAddress(InetAddress.getLoopbackAddress())
                .buildShutdownDelay(4)
                .buildRepositoryCreator(new MySQLRepositoryCreator());
    }
}
