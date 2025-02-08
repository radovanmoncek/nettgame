package client.game.docker.modules.examples.sessions.encoders;

import container.game.docker.modules.examples.session.models.SessionFlag;
import container.game.docker.modules.examples.session.models.SessionRequestProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class SessionRequestEncoder extends Encoder<SessionRequestProtocolDataUnit> {

    @Override
    protected void encodeBodyAfterHeader(final SessionRequestProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        switch (protocolDataUnit.sessionFlag()){

            case START, JOIN -> {

                final var nicknameByteBuf = ByteBufUtil.encodeString(out.alloc(), CharBuffer.wrap(protocolDataUnit.nickname()), Charset.defaultCharset());

                out
                        .writeByte(SessionFlag.START.ordinal())
                        .writeInt(protocolDataUnit.sessionHash())
                        .writeInt(protocolDataUnit.x())
                        .writeInt(protocolDataUnit.y())
                        .writeInt(protocolDataUnit.rotationAngle())
                        .writeBytes(nicknameByteBuf);
            }

            case STATE -> out
                        .writeByte(SessionFlag.STATE.ordinal())
                        .writeInt(protocolDataUnit.sessionHash())
                        .writeInt(protocolDataUnit.x())
                        .writeInt(protocolDataUnit.y())
                        .writeInt(protocolDataUnit.rotationAngle());


            case STOP -> out.writeByte(SessionFlag.STOP.ordinal());
        }
    }
}
