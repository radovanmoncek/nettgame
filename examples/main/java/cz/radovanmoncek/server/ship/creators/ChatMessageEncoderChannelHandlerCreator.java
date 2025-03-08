package cz.radovanmoncek.server.ship.creators;

import cz.radovanmoncek.server.modules.chats.codecs.ChatMessageFlatBuffersEncoder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import io.netty.channel.ChannelHandler;

public class ChatMessageEncoderChannelHandlerCreator extends ChannelHandlerCreator {

    @Override
    public ChannelHandler newProduct() {

        return new ChatMessageFlatBuffersEncoder();
    }
}
