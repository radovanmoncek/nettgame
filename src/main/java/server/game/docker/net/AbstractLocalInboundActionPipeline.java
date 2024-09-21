package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class AbstractLocalInboundActionPipeline implements LocalPipeline {
    @Override
    public ByteBuf encode(Object in) {return Unpooled.buffer(0);}
}
