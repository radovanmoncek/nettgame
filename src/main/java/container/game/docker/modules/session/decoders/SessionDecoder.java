package container.game.docker.modules.session.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import container.game.docker.modules.session.models.SessionProtocolDataUnit;

import java.util.List;

public final class SessionDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = in.readUnsignedByte();

        if(type != SessionProtocolDataUnit.PROTOCOL_IDENTIFIER){
            in.resetReaderIndex();
            ctx.fireChannelRead(in.retain());
            return;
        }

        if(in.readableBytes() < in.readLong()){
            in.resetReaderIndex();
            return;
        }

        out.add(new SessionProtocolDataUnit((byte) in.readUnsignedByte()));
    }
}
