package cz.radovanmoncek.ship.builders;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.bootstrap.GameServerBootstrap;
import cz.radovanmoncek.ship.parents.builders.Builder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.services.Service;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class GameServerBootstrapBuilder implements Builder<GameServerBootstrap> {
    private GameServerBootstrap result;

    public GameServerBootstrapBuilder() {

        result = GameServerBootstrap.newInstance();
    }

    @Override
    public GameServerBootstrap build() {

        return result;
    }

    /**
     * Please note that this method returns this Builder<T> in its generic ancestor form.
     * To use your specialization methods, you must do a polymorphic cast:
     * {@code (YourExampleBuilder) Builder<T>.reset()}.
     * @return this builder with a newly set result.
     */
    @Override
    public Builder<GameServerBootstrap> reset() {

        result = GameServerBootstrap.newInstance();

        return this;
    }

    public GameServerBootstrapBuilder buildPort(int port){

        result.setPort(port);

        return this;
    }

    public GameServerBootstrapBuilder buildInternetProtocolAddress(InetAddress address){

        result.setInternetProtocolAddress(address);

        return this;
    }

    public GameServerBootstrapBuilder buildChannelHandlerCreator(ChannelHandlerCreator channelHandlerCreator){

        result.addChannelHandlerCreator(channelHandlerCreator);

        return this;
    }

    public GameServerBootstrapBuilder buildPersistableClass(Class<?> persistableClass){

        result.addPersistableClass(persistableClass);

        return this;
    }

    public GameServerBootstrapBuilder buildService(Service<?> service){

        result.addService(service);

        return this;
    }

    public GameServerBootstrapBuilder buildProtocolSchema(
            final Byte magicByte,
            final Class<? extends Table> flatBufferSerializable
    ){

        result.registerMagicByteToFlatBufferSerializableBinding(magicByte, flatBufferSerializable);

        return this;
    }

    public GameServerBootstrapBuilder buildLogLevel(LogLevel logLevel) {

        result.setLogLevel(logLevel);

        return this;
    }
}
