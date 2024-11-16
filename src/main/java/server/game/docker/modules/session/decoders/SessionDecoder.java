package server.game.docker.modules.session.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.session.pdus.SessionPDU;

import java.util.List;

public class SessionDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = in.readUnsignedByte();

        if(type != SessionPDU.PROTOCOL_IDENTIFIER){
            in.resetReaderIndex();
//            ctx.fireChannelRead(in);
            return;
        }

        if(in.readableBytes() < in.readLong()){
            in.resetReaderIndex();
            return;
        }

        out.add(new SessionPDU((byte) in.readUnsignedByte()));
    }
}
