package cz.radovanmoncek.client.ship.bootstrap;

import cz.radovanmoncek.client.modules.games.handlers.ExampleServerChannelHandler;
import cz.radovanmoncek.client.ship.builders.GameClientBootstrapBuilder;
import cz.radovanmoncek.client.ship.directors.GameClientBootstrapDirector;

public final class ExampleGameClient {

    public static void main(String[] args) {

        new GameClientBootstrapDirector(new GameClientBootstrapBuilder())
                .makeDefaultGameClientBootstrapBuilder()
                .buildChannelHandler(new ExampleServerChannelHandler())
                .build()
                .run();
    }
}
