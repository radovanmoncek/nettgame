package cz.radovanmoncek.client.ship.directors;

import cz.radovanmoncek.client.modules.games.codecs.GameStateFlatBufferDecoder;
import cz.radovanmoncek.client.modules.games.codecs.GameStateRequestFlatBufferEncoder;
import cz.radovanmoncek.client.ship.builders.GameClientBootstrapBuilder;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class GameClientBootstrapDirector {
    private final GameClientBootstrapBuilder builder;

    public GameClientBootstrapDirector(GameClientBootstrapBuilder builder) {

        this.builder = builder;
    }

    public GameClientBootstrapBuilder makeDefaultGameClientBootstrapBuilder(){

        return builder
                .buildServerAddress(InetAddress.getLoopbackAddress())
                .buildPort(4321)
                .buildShutdownOnDisconnect(false)
                .buildLogLevel(LogLevel.INFO)
                .buildChannelHandler(new GameStateFlatBufferDecoder())
                .buildChannelHandler(new GameStateRequestFlatBufferEncoder())
                .buildReconnectAttempts(10)
                .buildReconnectDelay(4)
                .buildShutdownTimeout(4);
    }
}
