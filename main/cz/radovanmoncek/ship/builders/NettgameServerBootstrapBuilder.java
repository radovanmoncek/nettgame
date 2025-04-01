package cz.radovanmoncek.ship.builders;

import cz.radovanmoncek.ship.bootstrap.NettgameServerBootstrap;
import cz.radovanmoncek.ship.parents.builders.Builder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.repositories.Repository;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;
import java.util.logging.Logger;

public class NettgameServerBootstrapBuilder implements Builder<NettgameServerBootstrap> {
    private static final Logger logger = Logger.getLogger(NettgameServerBootstrapBuilder.class.getName());
    private NettgameServerBootstrap result;

    public NettgameServerBootstrapBuilder() {

        result = NettgameServerBootstrap.returnNewInstance();
    }

    @Override
    public NettgameServerBootstrap build() {

        return result;
    }

    /**
     * Attempts to <i>reflectively</i> reset the <i>singleton</i> {@link NettgameServerBootstrap} instance.
     * Please note that this method returns this Builder<T> in its generic ancestor form.
     * To use your specialization methods, you must do a polymorphic cast:
     * {@code (YourExampleBuilder) Builder<T>.reset()}.
     * @return this builder with a newly set result.
     */
    @Override
    public Builder<NettgameServerBootstrap> reset() {

        try {

            ReflectionUtilities.setValueOnFieldReflectively(NettgameServerBootstrap.class, "instance", null);

            result = NettgameServerBootstrap.returnNewInstance();
        }
        catch (IllegalAccessException | NoSuchFieldException exception) {

            logger.throwing(getClass().getName(), "reset", exception);
        }

        return this;
    }

    public NettgameServerBootstrapBuilder buildPort(int port){

        result.setPort(port);

        return this;
    }

    public NettgameServerBootstrapBuilder buildInternetProtocolAddress(InetAddress address){

        result.setInternetProtocolAddress(address);

        return this;
    }

    public NettgameServerBootstrapBuilder buildChannelHandlerCreator(ChannelHandlerCreator channelHandlerCreator){

        result.addChannelHandlerCreator(channelHandlerCreator);

        return this;
    }

    public NettgameServerBootstrapBuilder buildRepository(Repository<?> repository){

        result.addRepository(repository);

        return this;
    }

    public NettgameServerBootstrapBuilder buildLogLevel(LogLevel logLevel) {

        result.setLogLevel(logLevel);

        return this;
    }

    public NettgameServerBootstrapBuilder buildShutdownDelay(int i) {

        result.setShutdownTimeout(i);

        return this;
    }
}
