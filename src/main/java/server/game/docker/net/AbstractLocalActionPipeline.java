package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class AbstractLocalActionPipeline implements LocalPipeline {
    @Override
    public Object decode(ByteBuf in) {return null;}

    @Override
    public ByteBuf encode(Object in) {return Unpooled.buffer(0);}
}
