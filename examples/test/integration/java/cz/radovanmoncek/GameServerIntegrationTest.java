package cz.radovanmoncek;

import cz.radovanmoncek.ship.bootstrap.GameServerBootstrap;
import cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.creators.GameStateRequestFlatBuffersDecoderCreator;
import cz.radovanmoncek.server.ship.creators.ExampleGameSessionHandlerCreator;
import cz.radovanmoncek.server.ship.creators.GameStateFlatBuffersEncoderCreator;
import io.netty.handler.logging.LogLevel;
import org.junit.jupiter.api.BeforeAll;

public class GameServerIntegrationTest {
    private static GameServerBootstrap bootstrap;

    @BeforeAll
    static void setup(){

        bootstrap = new GameServerBootstrapBuilder()
                .buildLogLevel(LogLevel.INFO)
                .buildProtocolSchema((byte) 'G', GameState.class)
                .buildProtocolSchema((byte) 'g', GameStateRequest.class)
                .buildChannelHandlerCreator(new GameStateRequestFlatBuffersDecoderCreator())
                .buildChannelHandlerCreator(new ExampleGameSessionHandlerCreator())
                .buildChannelHandlerCreator(new GameStateFlatBuffersEncoderCreator())
                .build();
    }
}
