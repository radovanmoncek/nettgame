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
    private static final int MAX_PLAYER_NICKNAME_LENGTH = 8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = in.readByte();

        if(type != StateResponsePDU.PROTOCOL_IDENTIFIER){
            in.resetReaderIndex();
//            ctx.fireChannelRead(in.retain()); to extend modules
            return;
        }

        if(in.readableBytes() < in.readLong()){
            in.resetReaderIndex();
            return;
        }

        final var playerNickname= in.toString(in.readerIndex(), MAX_PLAYER_NICKNAME_LENGTH, Charset.defaultCharset());

        in.readerIndex(in.readerIndex() + MAX_PLAYER_NICKNAME_LENGTH);

        out.add(new StateResponsePDU(playerNickname, in.readInt(), in.readInt()));
    }
}
