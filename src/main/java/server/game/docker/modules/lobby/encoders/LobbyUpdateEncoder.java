package server.game.docker.modules.lobby.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.lobby.pdus.LobbyUpdatePDU;
import server.game.docker.ship.enums.PDUType;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public final class LobbyUpdateEncoder extends MessageToByteEncoder<LobbyUpdatePDU> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, LobbyUpdatePDU lobbyUpdate, ByteBuf out) {
        final var byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(PDUType.LOBBYUPDATE.oneBasedOrdinal())
                .writeLong(Byte.BYTES + Long.BYTES + (lobbyUpdate.getMembers() == null ? 0 : lobbyUpdate.getMembers().size() * (long) Long.BYTES))
                .writeByte(lobbyUpdate.getStateFlag())
                .writeLong(lobbyUpdate.getLeaderId() == null ? -1L : lobbyUpdate.getLeaderId());

        (lobbyUpdate.getMembers() == null ? new ArrayList<String>() : lobbyUpdate.getMembers())
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
