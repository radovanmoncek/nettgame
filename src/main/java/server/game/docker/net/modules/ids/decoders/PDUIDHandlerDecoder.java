package server.game.docker.net.modules.ids.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.modules.ids.pdus.PDUID;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;

public class PDUIDHandlerDecoder implements PDUHandlerDecoder {
    @Override
    public void decode(ByteBuf in, Channel channel, PDUInboundHandler out) {
        PDUID identifier = new PDUID();
        identifier.setNewClientID(in.readLong());
        out.handle(identifier);
    }
}
