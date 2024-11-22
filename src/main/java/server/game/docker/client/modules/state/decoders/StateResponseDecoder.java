package server.game.docker.client.modules.state.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.state.pdus.StateResponsePDU;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Example
 */
public class StateResponseDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if(in.markReaderIndex().readByte() != StateResponsePDU.PROTOCOL_IDENTIFIER){
            ctx.fireChannelRead(in.resetReaderIndex().retain());
            return;
        }

        if(in.readableBytes() < in.readLong()){
            in.resetReaderIndex();
            return;
        }

        final var playerNickname = in.toString(in.readerIndex(), StateResponsePDU.MAX_PLAYER_NICKNAME_LENGTH, Charset.defaultCharset());

        in.readerIndex(in.readerIndex() + StateResponsePDU.MAX_PLAYER_NICKNAME_LENGTH);

        out.add(new StateResponsePDU(playerNickname, in.readInt(), in.readInt()));
    }
}
