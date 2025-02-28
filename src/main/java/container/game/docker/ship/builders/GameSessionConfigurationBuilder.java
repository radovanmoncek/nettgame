package container.game.docker.ship.builders;

import container.game.docker.ship.enums.GameSessionConfigurationOption;
import container.game.docker.ship.parents.builders.Builder;
import container.game.docker.ship.parents.listeners.GameSessionListener;
import container.game.docker.ship.models.GameSessionConfiguration;
import io.netty.channel.Channel;

public class GameSessionConfigurationBuilder implements Builder<GameSessionConfiguration> {
    private GameSessionConfiguration result;

    @Override
    public GameSessionConfiguration build() {

        return result;
    }

    @Override
    public Builder<GameSessionConfiguration> reset() {

        result = null;

        return this;
    }

    public GameSessionConfigurationBuilder buildGameSessionUpdateListener(final GameSessionListener updateHandler) {

        result.setGameSessionUpdateHandler(updateHandler);

        return this;
    }

    public GameSessionConfigurationBuilder buildMaxPlayers(int maxPlayers) {

        result.setMaxPlayers(maxPlayers);

        return this;
    }

    public GameSessionConfigurationBuilder buildGameSessionConfigurationOption(final GameSessionConfigurationOption gameSessionConfigurationOption) {

        result.addGameSessionConfigurationOption(gameSessionConfigurationOption);

        return this;
    }

    public GameSessionConfigurationBuilder buildHostChannel(Channel hostChannel) {

        result.setHostChannel(hostChannel);

        return this;
    }
}
