package server.game.docker.client.modules.lobby.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;
import server.game.docker.ship.enums.PDUType;

public final class LobbyRequestEncoder extends MessageToByteEncoder<LobbyRequestPDU> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, LobbyRequestPDU in, ByteBuf out) {
        final var byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(PDUType.LOBBYREQUEST.oneBasedOrdinal())
                .writeLong(Byte.BYTES + (in.getActionFlag() == 1? Long.BYTES : 0))
                .writeByte(in.getActionFlag().intValue());
        if (in.getActionFlag() == 1) {
            byteBuf.writeLong(in.getLeaderId());
        }

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
