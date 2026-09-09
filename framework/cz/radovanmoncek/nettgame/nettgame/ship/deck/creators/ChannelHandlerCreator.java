package cz.radovanmoncek.nettgame.nettgame.ship.deck.creators;

import io.netty.channel.ChannelHandler;

/**
 * The Factory Method pattern for constructing different {@link ChannelHandler}s.
 * @since 1.0
 * @author Radovan Monček
 */
public interface ChannelHandlerCreator {

    /**
     * Construct some {@link ChannelHandler}.
     * @return the constructed {@link ChannelHandler}.
     * @since 1.0
     * @author Radovan Monček
     */
    ChannelHandler newProduct();
}
