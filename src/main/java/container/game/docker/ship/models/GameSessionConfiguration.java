package container.game.docker.ship.models;

import container.game.docker.ship.enums.GameSessionConfigurationOption;
import container.game.docker.ship.parents.listeners.GameSessionListener;
import io.netty.channel.Channel;

import java.util.Set;

/**
 * Inspired by the Jetty Java server
 */
public class GameSessionConfiguration {
    private GameSessionListener gameSessionListener;
    private int maxPlayers;
    private Set<GameSessionConfigurationOption> options;
    private Channel hostChannel;

    public GameSessionListener getGameSessionUpdateHandler() {

        return gameSessionListener;
    }

    public void setGameSessionUpdateHandler(final GameSessionListener gameSessionListener) {

        this.gameSessionListener = gameSessionListener;
    }

    public int getMaxPlayers() {

        return maxPlayers;
    }

    public void setMaxPlayers(final int maxPlayers) {

        this.maxPlayers = maxPlayers;
    }

    public void addGameSessionConfigurationOption(final GameSessionConfigurationOption gameSessionConfigurationOption) {

        options.add(gameSessionConfigurationOption);
    }

    public boolean containsOption(final GameSessionConfigurationOption gameSessionConfigurationOption) {
        return options.contains(gameSessionConfigurationOption);
    }

    public void setHostChannel(Channel hostChannel) {

        this.hostChannel = hostChannel;
    }

    public Channel getHostChannel() {

        return hostChannel;
    }
}
