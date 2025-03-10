package cz.radovanmoncek.ship.directors;

import cz.radovanmoncek.client.ship.builders.GameClientBootstrapBuilder;
import cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class GameServerBootstrapDirector {
    private final GameServerBootstrapBuilder builder;

    public GameServerBootstrapDirector(GameServerBootstrapBuilder builder) {
        this.builder = builder;
    }

    public GameServerBootstrapBuilder makeDefaultGameServerBootstrap() {

        return builder
                .buildLogLevel(LogLevel.INFO)
                .buildPort(4321)
                .buildInternetProtocolAddress(InetAddress.getLoopbackAddress())
                .buildShutdownDelay(4);
    }
}
