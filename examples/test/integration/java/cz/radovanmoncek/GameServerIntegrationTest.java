package cz.radovanmoncek;

import cz.radovanmoncek.ship.bootstrap.GameServerBootstrap;
import cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.creators.GameStateRequestFlatBuffersDecoderCreator;
import cz.radovanmoncek.server.ship.creators.ExampleGameSessionHandlerCreator;
import cz.radovanmoncek.server.ship.creators.GameStateFlatBuffersEncoderCreator;
import cz.radovanmoncek.ship.directors.GameServerBootstrapDirector;
import io.netty.handler.logging.LogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GameServerIntegrationTest {
    private static GameServerBootstrap bootstrap;

    @BeforeAll
    static void setup(){

        bootstrap = new GameServerBootstrapDirector(new GameServerBootstrapBuilder())
                .makeDefaultGameServerBootstrap()
                .buildChannelHandlerCreator(new GameStateRequestFlatBuffersDecoderCreator())
                .buildChannelHandlerCreator(new ExampleGameSessionHandlerCreator())
                .buildChannelHandlerCreator(new GameStateFlatBuffersEncoderCreator())
                .build();
    }

    @Test
    void runTest(){

        bootstrap.run();
    }
}
