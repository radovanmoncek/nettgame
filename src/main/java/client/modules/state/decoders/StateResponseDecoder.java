package client.modules.state.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.state.pdus.StateResponseProtocolDataUnit;

import java.util.List;

/**
 * Example
 */
public class StateResponseDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if(in.markReaderIndex().readByte() != StateResponseProtocolDataUnit.PROTOCOL_IDENTIFIER){
            ctx.fireChannelRead(in.resetReaderIndex().retain());
            return;
        }

        if(in.readableBytes() < in.readLong()){
            in.resetReaderIndex();
            return;
        }

        out.add(new StateResponseProtocolDataUnit(in.readInt(), in.readInt(), 0, in.readInt(), in.readInt()));
    }
}
