package server.game.docker.client.modules.requests.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.GameServerInitializer;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.ship.parents.pdus.PDU;

public class PDULobbyRequestEncoder implements GameServerInitializer.PDUHandlerEncoder {
    @Override
    public void encode(PDU in, Channel out) {
        PDULobbyReq lobbyReq = (PDULobbyReq) in;
        ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                .writeByte(PDUType.LOBBYREQUEST.oneBasedOrdinal())
                .writeLong(Byte.BYTES + Long.BYTES)
                .writeByte(lobbyReq.getActionFlag().intValue());
        if (lobbyReq.getActionFlag() == 1) {
            byteBuf.writeLong(lobbyReq.getLobbyID());
        }

        out.writeAndFlush(byteBuf);
    }
}
