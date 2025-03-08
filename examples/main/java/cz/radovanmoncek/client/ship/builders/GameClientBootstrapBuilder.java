package cz.radovanmoncek.client.ship.builders;

import cz.radovanmoncek.client.ship.bootstrap.GameClientBootstrap;
import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.parents.builders.Builder;
import io.netty.channel.ChannelHandler;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class GameClientBootstrapBuilder implements Builder<GameClientBootstrap> {
    private GameClientBootstrap result;

    public GameClientBootstrapBuilder() {

        result = GameClientBootstrap.newInstance();
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

        result.setServerPort(port);

        return this;
    }

    public GameClientBootstrapBuilder buildServerAddress(InetAddress address){

        result.setInstanceContainerAddress(address);

        return this;
    }

    public GameClientBootstrapBuilder buildChannelHandler(ChannelHandler channelHandler){

        result.addChannelHandler(channelHandler);

        return this;
    }

    public GameClientBootstrapBuilder buildMagicByteToFlatBuffersSchemaBinding(final Byte magicByte, Class<? extends Table> flatBuffersSchema){

        result.registerProtocolDataUnitToProtocolDataUnitBinding(magicByte, flatBuffersSchema);

        return this;
    }

    public GameClientBootstrapBuilder buildLogLevel(LogLevel logLevel) {

        result.setLogLevel(logLevel);

        return this;
    }
}
