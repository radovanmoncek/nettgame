package server.game.docker.net.modules.requests.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.modules.requests.pdus.PDULobbyReq;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;

public class PDULobbyRequestDecoder implements PDUHandlerDecoder {
    @Override
    public void decode(ByteBuf byteBuf, Channel channel, PDUInboundHandler handler) {
        PDULobbyReq lobbyReq = new PDULobbyReq();
        lobbyReq.setActionFlag(byteBuf.readByte());
        lobbyReq.setLobbyID(byteBuf.readLong());
        handler.handle(lobbyReq, channel);
    }
}
