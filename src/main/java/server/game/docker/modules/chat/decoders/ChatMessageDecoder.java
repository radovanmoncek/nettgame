package server.game.docker.modules.chat.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;

import java.nio.charset.Charset;
import java.util.List;

public final class ChatMessageDecoder extends ByteToMessageDecoder {
    private static final int AUTHOR_NAME_LENGTH = 8;
    private static final int MAX_MESSAGE_LENGTH = 64;

    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        final var chatMessage = new ChatMessagePDU();

        chatMessage.setAuthorID(in.readLong());
        chatMessage.setAuthorName(in.toString(in.readerIndex(), AUTHOR_NAME_LENGTH, Charset.defaultCharset()).trim());
        chatMessage.setMessage(in.toString(in.readerIndex() + AUTHOR_NAME_LENGTH, MAX_MESSAGE_LENGTH, Charset.defaultCharset()).trim());

        out.add(chatMessage);
    }
}
