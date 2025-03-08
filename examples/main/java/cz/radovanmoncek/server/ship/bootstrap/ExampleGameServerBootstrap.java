package cz.radovanmoncek.server.ship.bootstrap;

import cz.radovanmoncek.server.modules.games.models.GameHistory;
import cz.radovanmoncek.server.modules.games.services.ExampleGameHistoryService;
import cz.radovanmoncek.server.ship.creators.GameStateRequestFlatBuffersDecoderCreator;
import cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.ChatMessage;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.creators.ExampleGameSessionHandlerCreator;
import cz.radovanmoncek.server.ship.creators.GameStateFlatBuffersEncoderCreator;
import cz.radovanmoncek.ship.directors.GameServerBootstrapDirector;

public final class ExampleGameServerBootstrap {

    public static void main(String[] args) {

        new GameServerBootstrapDirector(new GameServerBootstrapBuilder())
                .makeDefaultGameServerBootstrap()
                .buildChannelHandlerCreator(new GameStateRequestFlatBuffersDecoderCreator())
                .buildChannelHandlerCreator(new ExampleGameSessionHandlerCreator())
                .buildChannelHandlerCreator(new GameStateFlatBuffersEncoderCreator())
                .buildProtocolSchema((byte) 'G', GameState.class)
                .buildProtocolSchema((byte) 'g', GameStateRequest.class)
                .buildProtocolSchema((byte) 'm', ChatMessage.class)
                .buildPersistableClass(GameHistory.class)
                .buildService(new ExampleGameHistoryService())
                .build()
                .run();
    }
}
