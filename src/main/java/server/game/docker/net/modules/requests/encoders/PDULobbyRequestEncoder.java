package server.game.docker.net.modules.requests.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.requests.pdus.PDULobbyReq;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.pdus.PDU;

public class PDULobbyRequestEncoder implements PDUHandlerEncoder {
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
