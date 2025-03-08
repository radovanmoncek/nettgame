package cz.radovanmoncek.server.ship.creators;

import cz.radovanmoncek.server.modules.chats.codecs.ChatMessageFlatBuffersDecoder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import io.netty.channel.ChannelHandler;

public class ChatMessageDecoderChannelHandlerCreator extends ChannelHandlerCreator {

    @Override
    public ChannelHandler newProduct() {

        return new ChatMessageFlatBuffersDecoder();
    }
}
