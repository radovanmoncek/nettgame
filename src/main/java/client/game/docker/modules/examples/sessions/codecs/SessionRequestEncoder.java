package client.game.docker.modules.examples.sessions.codecs;

import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class SessionRequestEncoder extends Encoder<SessionRequestProtocolDataUnit> {

    @Override
    protected void encodeBodyAfterHeader(final SessionRequestProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        out.writeByte(protocolDataUnit.sessionFlag().ordinal());

        switch (protocolDataUnit.sessionFlag()){

            case START -> encodeString(protocolDataUnit.nickname(), out);

            case STATE -> out
                        .writeInt(protocolDataUnit.x())
                        .writeInt(protocolDataUnit.y())
                        .writeInt(protocolDataUnit.rotationAngle());

            case JOIN -> {

                encodeString(protocolDataUnit.nickname(), out);
                encodeString(protocolDataUnit.sessionUUID(), out);
            }
        }
    }
}
