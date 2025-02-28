package container.game.docker.ship.builders;

import com.google.flatbuffers.Table;
import container.game.docker.ship.bootstrap.InstanceContainerBootstrap;
import container.game.docker.ship.parents.builders.Builder;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.models.FlatBufferSerializable;
import container.game.docker.ship.parents.services.Service;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class InstanceContainerBootstrapBuilder implements Builder<InstanceContainerBootstrap> {
    private InstanceContainerBootstrap result;

    public InstanceContainerBootstrapBuilder() {

        result = InstanceContainerBootstrap.newInstance();
    }

    @Override
    public InstanceContainerBootstrap build() {

        return result;
    }

    @Override
    public Builder<InstanceContainerBootstrap> reset() {

        result = null;

        return this;
    }

    public InstanceContainerBootstrapBuilder buildPort(int port){

        result.setPort(port);

        return this;
    }

    public InstanceContainerBootstrapBuilder buildInternetProtocolAddress(InetAddress address){

        result.setInternetProtocolAddress(address);

        return this;
    }

    public InstanceContainerBootstrapBuilder buildChannelHandlerCreator(Creator channelHandlerCreator){

        result.addChannelHandlerCreator(channelHandlerCreator);

        return this;
    }

    public InstanceContainerBootstrapBuilder buildPersistableClass(Class<?> persistableClass){

        result.addPersistableClass(persistableClass);

        return this;
    }

    public InstanceContainerBootstrapBuilder buildService(Service<?> service){

        result.addService(service);

        return this;
    }

    public InstanceContainerBootstrapBuilder buildProtocolSchema(
            final Byte magicByte,
            final Class<? extends Table> flatBufferSerializable
    ){

        result.registerMagicByteToFlatBufferSerializableBinding(magicByte, flatBufferSerializable);

        return this;
    }

    public InstanceContainerBootstrapBuilder buildLogLevel(LogLevel logLevel) {

        result.setLogLevel(logLevel);

        return this;
    }
}
