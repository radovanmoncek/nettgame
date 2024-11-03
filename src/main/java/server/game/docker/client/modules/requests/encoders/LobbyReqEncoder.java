package server.game.docker.client.modules.requests.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.ship.enums.PDUType;

public final class LobbyReqEncoder extends MessageToByteEncoder<PDULobbyReq> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PDULobbyReq in, ByteBuf out) {
        final var byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(PDUType.LOBBYREQUEST.oneBasedOrdinal())
                .writeLong(Byte.BYTES + (in.getActionFlag() == 1? Long.BYTES : 0))
                .writeByte(in.getActionFlag().intValue());
        if (in.getActionFlag() == 1) {
            byteBuf.writeLong(in.getLobbyID());
        }

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
