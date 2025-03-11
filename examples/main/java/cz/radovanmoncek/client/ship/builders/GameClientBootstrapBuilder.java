package cz.radovanmoncek.client.ship.builders;

import cz.radovanmoncek.client.ship.bootstrap.GameClientBootstrap;
import cz.radovanmoncek.ship.parents.builders.Builder;
import io.netty.channel.ChannelHandler;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class GameClientBootstrapBuilder implements Builder<GameClientBootstrap> {
    private GameClientBootstrap result;

    public GameClientBootstrapBuilder() {

        result = GameClientBootstrap.returnNewInstance();
    }

    @Override
    public GameClientBootstrap build() {

        return result;
    }

    @Override
    public Builder<GameClientBootstrap> reset() {

        result = null;

        return this;
    }

    public GameClientBootstrapBuilder buildShutdownOnDisconnect(final boolean shutdownOnDisconnect) {

        result.setShutdownOnDisconnect(shutdownOnDisconnect);

        return this;
    }

    public GameClientBootstrapBuilder buildPort(int port){

        result.setGameServerPort(port);

        return this;
    }

    public GameClientBootstrapBuilder buildServerAddress(InetAddress address) {

        result.setGameServerAddress(address);

        return this;
    }

    public GameClientBootstrapBuilder buildChannelHandler(ChannelHandler channelHandler) {

        result.addChannelHandler(channelHandler);

        return this;
    }

    public GameClientBootstrapBuilder buildLogLevel(LogLevel logLevel) {

        result.setLogLevel(logLevel);

        return this;
    }

    public GameClientBootstrapBuilder buildReconnectAttempts(int i) {

        result.setReconnectAttempts(i);

        return this;
    }

    public GameClientBootstrapBuilder buildReconnectDelay(int i) {

        result.setReconnectDelay(i);

        return this;
    }

    public GameClientBootstrapBuilder buildShutdownTimeout(int i) {

        result.setShutdownTimeout(i);

        return this;
    }
}
