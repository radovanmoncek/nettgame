package client.game.docker.ship.bootstrap.examples;

import client.game.docker.ship.builders.GameClientBootstrapBuilder;

public final class SampleGameClient {

    public static void main(String[] args) {

        new GameClientBootstrapBuilder()
                .build()
                .run();
    }
}
