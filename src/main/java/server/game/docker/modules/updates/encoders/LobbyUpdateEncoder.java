package server.game.docker.modules.updates.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import server.game.docker.modules.updates.pdus.PDULobbyUpdate;
import server.game.docker.ship.enums.PDUType;

import java.util.ArrayList;
import java.util.List;

public final class LobbyUpdateEncoder extends MessageToMessageEncoder<PDULobbyUpdate> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PDULobbyUpdate lobbyUpdate, List<Object> out) {
        ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(PDUType.LOBBYUPDATE.oneBasedOrdinal())
                .writeLong(Long.BYTES + 2 * Byte.BYTES + (lobbyUpdate.getMembers() == null ? 0 : lobbyUpdate.getMembers().size() * (long) Long.BYTES))
                .writeByte(lobbyUpdate.getStateFlag())
                .writeLong(lobbyUpdate.getLobbyId() == null ? -1L : lobbyUpdate.getLobbyId())
                .writeBoolean(lobbyUpdate.isLeader() != null && lobbyUpdate.isLeader());

        (lobbyUpdate.getMembers() == null ? new ArrayList<Long>() : lobbyUpdate.getMembers()).forEach(byteBuf::writeLong);

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
