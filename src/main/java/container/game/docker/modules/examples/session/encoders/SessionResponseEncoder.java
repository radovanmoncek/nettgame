package container.game.docker.modules.examples.session.encoders;

import container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler;
import container.game.docker.modules.examples.session.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import container.game.docker.modules.examples.session.models.SessionRequestProtocolDataUnit;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Example.
 */
public class SessionResponseEncoder extends Encoder<SessionResponseProtocolDataUnit> {

    @Override
    protected void encodeBodyAfterHeader(final SessionResponseProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        final var usernameByteBuffer = ByteBufUtil
                .encodeString(
                        out.alloc(),
                        CharBuffer.wrap(protocolDataUnit.nickname1()),
                        Charset.defaultCharset()
                );

        out
                .writeByte(protocolDataUnit.sessionFlag().ordinal())
                .writeInt(protocolDataUnit.sessionHash())
                .writeInt(protocolDataUnit.x1())
                .writeInt(protocolDataUnit.y1())
                .writeInt(protocolDataUnit.rotationAngle1())
                .writeInt(protocolDataUnit.x2())
                .writeInt(protocolDataUnit.y2())
                .writeInt(protocolDataUnit.rotationAngle2())
                .writeBytes(usernameByteBuffer);
    }
}
