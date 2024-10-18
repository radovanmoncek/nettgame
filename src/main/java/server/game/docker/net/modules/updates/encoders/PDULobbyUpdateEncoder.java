package server.game.docker.net.modules.updates.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.updates.pdus.PDULobbyUpdate;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.pdus.PDU;

import java.util.ArrayList;

public class PDULobbyUpdateEncoder implements PDUHandlerEncoder {
    @Override
    public void encode(PDU in, Channel channel) {
        PDULobbyUpdate lobbyUpdate = (PDULobbyUpdate) in;
        ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(PDUType.LOBBYUPDATE.oneBasedOrdinal())
                .writeLong(Long.BYTES + 2 * Byte.BYTES + (lobbyUpdate.getMembers() == null ? 0 : lobbyUpdate.getMembers().size() * (long) Long.BYTES))
                .writeByte(lobbyUpdate.getStateFlag())
                .writeLong(lobbyUpdate.getLobbyId() == null ? -1L : lobbyUpdate.getLobbyId())
                .writeBoolean(lobbyUpdate.isLeader() != null && lobbyUpdate.isLeader());

        (lobbyUpdate.getMembers() == null ? new ArrayList<Long>() : lobbyUpdate.getMembers()).forEach(byteBuf::writeLong);

        channel.writeAndFlush(byteBuf);
    }
}
