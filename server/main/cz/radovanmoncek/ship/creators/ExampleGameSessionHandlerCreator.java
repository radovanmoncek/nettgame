package cz.radovanmoncek.ship.creators;

import cz.radovanmoncek.modules.games.handlers.ExampleGameSessionChannelGroupHandler;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import io.netty.channel.ChannelHandler;

public class ExampleGameSessionHandlerCreator extends ChannelHandlerCreator {

    @Override
    public ChannelHandler newProduct() {

        return new ExampleGameSessionChannelGroupHandler();
    }
}
