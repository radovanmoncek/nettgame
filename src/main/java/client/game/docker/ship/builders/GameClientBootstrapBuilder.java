package client.game.docker.ship.builders;

import client.game.docker.ship.bootstrap.GameClientBootstrap;
import client.game.docker.ship.parents.handlers.ServerChannelHandler;
import com.google.flatbuffers.Table;
import container.game.docker.ship.parents.builders.Builder;
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

    public GameClientBootstrapBuilder buildPort(int port){

        result.withServerPort(port);

        return this;
    }

    public GameClientBootstrapBuilder buildServerAddress(InetAddress address){

        result.withInstanceContainerAddress(address);

        return this;
    }

    public GameClientBootstrapBuilder buildChannelHandler(ChannelHandler channelHandler){

        result.withChannelHandler(channelHandler);

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
