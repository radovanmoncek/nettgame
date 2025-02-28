package container.game.docker.ship.directors;

import container.game.docker.ship.builders.GameSessionConfigurationBuilder;

public class GameSessionConfigurationDirector {
    private final GameSessionConfigurationBuilder builder;

    public GameSessionConfigurationDirector(GameSessionConfigurationBuilder builder) {

        this.builder = builder;
    }

    public GameSessionConfigurationBuilder makeDefaultGameSessionConfiguration() {

        return builder;
    }

    public GameSessionConfigurationBuilder make2PlayerGameSessionConfiguration() {

        return builder
                .buildMaxPlayers(2);
    }
}
