package server.game.docker.modules.session.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.session.pdus.SessionProtocolDataUnit;

public class SessionEncoder extends MessageToByteEncoder<SessionProtocolDataUnit> {
    @Override
    protected void encode(ChannelHandlerContext ctx, SessionProtocolDataUnit msg, ByteBuf out) {
        final var outBytes = Unpooled
                .buffer(Byte.BYTES + Long.BYTES)
                .writeByte(SessionProtocolDataUnit.PROTOCOL_IDENTIFIER)
                .writeLong(Byte.BYTES)
                .writeByte(msg.sessionFlag());

        ctx.writeAndFlush(outBytes);
    }
}
