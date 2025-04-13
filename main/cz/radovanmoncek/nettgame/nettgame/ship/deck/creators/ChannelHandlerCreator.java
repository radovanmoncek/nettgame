package cz.radovanmoncek.nettgame.nettgame.ship.deck.creators;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerCreator {

    ChannelHandler newProduct();
}
