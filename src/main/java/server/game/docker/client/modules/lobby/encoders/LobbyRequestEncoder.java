package server.game.docker.client.modules.lobby.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;

public final class LobbyRequestEncoder extends MessageToByteEncoder<LobbyRequestPDU> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, LobbyRequestPDU in, ByteBuf out) {
        final var byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(LobbyRequestPDU.PROTOCOL_IDENTIFIER)
                .writeLong(Byte.BYTES + (in.lobbyRequestFlag() == LobbyRequestPDU.LobbyRequestFlag.JOIN.ordinal()? Long.BYTES : 0))
                .writeByte(in.lobbyRequestFlag());
        if (in.lobbyRequestFlag() == LobbyRequestPDU.LobbyRequestFlag.JOIN.ordinal()) {
            byteBuf.writeLong(in.leaderId());
        }

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
