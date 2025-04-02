package cz.radovanmoncek.ship.bay.parents.creators;

import io.netty.channel.ChannelHandler;

public abstract class ChannelHandlerCreator {

    public abstract ChannelHandler newProduct();
}
