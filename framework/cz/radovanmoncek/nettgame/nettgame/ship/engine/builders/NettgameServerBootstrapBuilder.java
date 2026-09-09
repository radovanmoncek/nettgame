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

    /**
     * Build a specific port.
     * @param port the port to build.
     * @return this for chaining.
     */
    public NettgameServerBootstrapBuilder buildPort(int port){

        result.setPort(port);

        return this;
    }

    /**
     * build a specific address.
     * @param address the address to build.
     * @return this for chaining.
     */
    public NettgameServerBootstrapBuilder buildInternetProtocolAddress(InetAddress address){

        result.setInternetProtocolAddress(address);

        return this;
    }

    /**
     * build a handler for business logic.
     * @param channelHandlerCreator the handler to build.
     * @return this for chaining.
     */
    public NettgameServerBootstrapBuilder buildChannelHandlerCreator(ChannelHandlerCreator channelHandlerCreator){

        result.addChannelHandlerCreator(channelHandlerCreator);

        return this;
    }

    /**
     * build a repository for use in handleres.
     * @param repository the repository to build.
     * @return this for chaining.
     */
    public NettgameServerBootstrapBuilder buildRepository(Repository<?> repository){

        result.addRepository(repository);

        return this;
    }

    /**
     * build a handler with a specific logging level.
     * @param logLevel the level for the logging handler.
     * @return this for chaining.
     */
    public NettgameServerBootstrapBuilder buildLogLevel(LogLevel logLevel) {

        result.setLogLevel(logLevel);

        return this;
    }

    /**
     * build a shutdown delay value.
     * @param i the ammount of seconds to defer shutdown for.
     * @return this for chaining.
     */
    public NettgameServerBootstrapBuilder buildShutdownDelay(int i) {

        result.setShutdownTimeout(i);

        return this;
    }
}
