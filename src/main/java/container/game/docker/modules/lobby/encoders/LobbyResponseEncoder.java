package server.game.docker.modules.lobby.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.lobby.pdus.LobbyResponseProtocolDataUnit;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public final class LobbyResponseEncoder extends MessageToByteEncoder<LobbyResponseProtocolDataUnit> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, LobbyResponseProtocolDataUnit lobbyUpdate, ByteBuf out) {
        final var byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(LobbyResponseProtocolDataUnit.PROTOCOL_IDENTIFIER)
                .writeLong(Byte.BYTES + Long.BYTES + (lobbyUpdate.members() == null ? 0 : lobbyUpdate.members().size() * (long) Long.BYTES))
                .writeByte(lobbyUpdate.lobbyUpdateResponseFlag())
                .writeLong(lobbyUpdate.leaderId() == null ? -1L : lobbyUpdate.leaderId());

        (lobbyUpdate.members() == null ? new ArrayList<String>() : lobbyUpdate.members())
                .forEach(member -> byteBuf.writeBytes(
                        ByteBufUtil.encodeString(
                                Unpooled.buffer(8).alloc(),
                                CharBuffer.allocate(8).append(member).position(0),
                                Charset.defaultCharset()
                        )
                ));

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
