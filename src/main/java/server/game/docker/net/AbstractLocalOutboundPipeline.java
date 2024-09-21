package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import server.game.docker.net.pdu.PDU;

public abstract class AbstractLocalOutboundPipeline implements LocalPipeline{
    @Override
    public Object decode(ByteBuf in) {return null;}

    @Override
    public void perform(PDU p) {}
}
