package server.game.docker.modules.lobby.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;
import server.game.docker.ship.enums.PDUType;

import java.util.List;

public final class LobbyRequestDecoder extends ByteToMessageDecoder {
    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = PDUType.valueOf((byte) in.readUnsignedByte());

        if(!type.equals(PDUType.LOBBYREQUEST)) {
            in.resetReaderIndex();
//            channelHandlerContext.fireChannelRead(in);
            return;
        }

        if((in.readableBytes() < in.readLong())){
            in.resetReaderIndex();
            return;
        }

        final var lobbyReq = new LobbyRequestPDU();
        lobbyReq.setActionFlag(in.readByte());
        if(lobbyReq.getActionFlag().equals(LobbyRequestPDU.JOIN))
            lobbyReq.setLeaderId(in.readLong());
        out.add(lobbyReq);
    }
}
