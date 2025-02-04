package container.game.docker.modules.chat.examples.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.chat.pdus.ChatMessageProtocolDataUnit;

import java.nio.charset.Charset;
import java.util.List;

public final class ChatMessageDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if(in.markReaderIndex().readByte() != ChatMessageProtocolDataUnit.PROTOCOL_IDENTIFIER){
            return;
        }

        if(in.readableBytes() < in.readLong()){
            in.resetReaderIndex();
            return;
        }

        out.add(
                new ChatMessageProtocolDataUnit(
                        in
                                .toString(in.readerIndex(), ChatMessageProtocolDataUnit.AUTHOR_NICK_LENGTH, Charset.defaultCharset())
                                .trim(),
                        in
                                .toString(
                                        in.readerIndex() + ChatMessageProtocolDataUnit.AUTHOR_NICK_LENGTH,
                                        ChatMessageProtocolDataUnit.MAX_MESSAGE_LENGTH,
                                        Charset.defaultCharset())
                                .trim()
                )
        );
    }
}
