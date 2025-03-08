package cz.radovanmoncek.server.ship.creators;

import cz.radovanmoncek.server.modules.chats.handlers.ExampleGameChatHandler;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import io.netty.channel.ChannelHandler;

public class ChatChannelGroupHandlerChannelHandlerCreator extends ChannelHandlerCreator {

    @Override
    public ChannelHandler newProduct() {

        return new ExampleGameChatHandler();
    }
}
