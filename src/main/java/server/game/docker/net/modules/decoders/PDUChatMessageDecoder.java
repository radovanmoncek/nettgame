package server.game.docker.net.modules.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.modules.pdus.PDUChatMessage;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;

import java.nio.charset.Charset;

public class PDUChatMessageDecoder implements PDUHandlerDecoder {
    private static final int AUTHOR_NAME_LENGTH = 8;
    private static final int MAX_MESSAGE_LENGTH = 64;

    @Override
    public void decode(ByteBuf in, Channel channel, PDUInboundHandler out) {
        PDUChatMessage chatMessage = new PDUChatMessage();

        chatMessage.setAuthorID(in.readLong());
        chatMessage.setAuthorName(in.toString(in.readerIndex(), AUTHOR_NAME_LENGTH, Charset.defaultCharset()));
        chatMessage.setMessage(in.toString(in.readerIndex() + AUTHOR_NAME_LENGTH, MAX_MESSAGE_LENGTH, Charset.defaultCharset()));

        out.handle(chatMessage, channel);
    }
}
