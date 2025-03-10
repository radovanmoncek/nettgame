package cz.radovanmoncek.server.ship.bootstrap;

import cz.radovanmoncek.server.modules.games.models.ExampleGameHistoryPersistableModel;
import cz.radovanmoncek.server.ship.creators.GameStateRequestFlatBuffersDecoderCreator;
import cz.radovanmoncek.ship.builders.GameServerBootstrapBuilder;
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
                .buildPersistableModel(new ExampleGameHistoryPersistableModel())
                .build()
                .run();
    }
}
