package server.game.docker.modules.state.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.client.modules.state.pdus.StateRequestPDU;

import java.util.List;

public class StateRequestDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = in.readUnsignedByte();

        if(type != StateRequestPDU.PROTOCOL_IDENTIFIER){
            in.resetReaderIndex();
            ctx.fireChannelRead(in.retain());
            return;
        }

        if(in.readableBytes() < in.readLong()){
            in.resetReaderIndex();
            return;
        }

        out.add(new StateRequestPDU(in.readInt(), in.readInt()));
    }
}
