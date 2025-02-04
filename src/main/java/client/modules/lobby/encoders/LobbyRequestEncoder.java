package client.modules.lobby.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.lobby.pdus.LobbyRequestProtocolDataUnit;

public final class LobbyRequestEncoder extends MessageToByteEncoder<LobbyRequestProtocolDataUnit> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, LobbyRequestProtocolDataUnit in, ByteBuf out) {
        final var byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(LobbyRequestProtocolDataUnit.PROTOCOL_IDENTIFIER)
                .writeLong(Byte.BYTES + (in.lobbyRequestFlag() == LobbyRequestProtocolDataUnit.LobbyRequestFlag.JOIN.ordinal()? Long.BYTES : 0))
                .writeByte(in.lobbyRequestFlag());
        if (in.lobbyRequestFlag() == LobbyRequestProtocolDataUnit.LobbyRequestFlag.JOIN.ordinal()) {
            byteBuf.writeLong(in.leaderId());
        }

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
