package server.game.docker.net.modules.ids.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.ids.pdus.PDUID;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.pdus.PDU;

public class PDUIDHandlerEncoder implements PDUHandlerEncoder {
    @Override
    public void encode(PDU in, Channel out) {
        PDUID iD = (PDUID) in;
        ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES)
                .writeByte(PDUType.ID.oneBasedOrdinal())
                .writeLong(Long.BYTES)
                .writeLong(iD.getNewClientID());
        out.writeAndFlush(byteBuf);
    }
}
