package cz.radovanmoncek.nettgame.nettgame.ship.engine.builders;

import cz.radovanmoncek.nettgame.nettgame.ship.engine.bootstrap.NettgameServerBootstrap;
import cz.radovanmoncek.nettgame.nettgame.ship.deck.builders.Builder;
import cz.radovanmoncek.nettgame.nettgame.ship.deck.creators.ChannelHandlerCreator;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.repositories.Repository;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.utilities.reflection.ReflectionUtilities;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Configures and builds a {@link NettgameServerBootstrap} instance.
 *
 * @apiNote uses the Builder design pattern.
 * @author Radovan Monček
 * @since 1.0
 */
public class NettgameServerBootstrapBuilder implements Builder<NettgameServerBootstrap> {
    private static final Logger logger = Logger.getLogger(NettgameServerBootstrapBuilder.class.getName());
    private NettgameServerBootstrap result;

    /**
     * Constructs a new instance.
     */
    public NettgameServerBootstrapBuilder() {

        result = NettgameServerBootstrap.returnNewInstance();
    }

    /**
     * Build the configured instance.
     *
     * @return the configured instance.
     */
    @Override
    public NettgameServerBootstrap build() {

        return result;
    }

    /**
     * Attempts to <i>reflectively</i> reset the <i>singleton</i> {@link NettgameServerBootstrap} instance.
     *
     * @apiNote Please note that this method returns this {@code Builder<T>} in its generic ancestor form.
     * To use your specialization methods, you must do a polymorphic cast:
     * {@code (YourExampleBuilder) Builder<T>.reset()}.
     * @return {@code this} builder with a newly set result.
     * @author Radovan Monček
     * @since 1.0
     */
    @Override
    public Builder<NettgameServerBootstrap> reset() {

        try {

            ReflectionUtilities.setValueOnFieldReflectively(NettgameServerBootstrap.class, "instance", null);

            result = NettgameServerBootstrap.returnNewInstance();
        }
        catch (final IllegalAccessException | NoSuchFieldException exception) {

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
