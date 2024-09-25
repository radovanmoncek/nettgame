package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import server.game.docker.net.pdu.PDU;

public class DefaultLocalPipeline implements LocalPipeline {
    @Override
    public Object decode(ByteBuf in) {return null;}

    @Override
    public ByteBuf encode(Object in) {return Unpooled.buffer(0);}

    @Override
    public void handle(PDU p) {}
}
