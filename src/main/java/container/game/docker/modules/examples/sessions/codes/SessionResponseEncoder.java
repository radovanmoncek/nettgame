package container.game.docker.modules.examples.sessions.codes;

import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Example.
 */
public class SessionResponseEncoder extends Encoder<SessionResponseProtocolDataUnit> {

    public SessionResponseEncoder(final Map<Class<? extends ProtocolDataUnit>, Byte> protocolDataUnitToProtocolIdentifierBindings) {

        super(protocolDataUnitToProtocolIdentifierBindings);
    }

    @Override
    protected void encodeBodyAfterHeader(final SessionResponseProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        out.writeByte(protocolDataUnit.sessionFlag().ordinal());

        switch(protocolDataUnit.sessionFlag()){

            case START -> {

                encodeString(protocolDataUnit.nickname1(), out);
                encodeString(protocolDataUnit.sessionUUID(), out);
            }

            case STATE -> out
                        .writeInt(protocolDataUnit.x1())
                        .writeInt(protocolDataUnit.y1())
                        .writeInt(protocolDataUnit.rotationAngle1())
                        .writeInt(protocolDataUnit.x2())
                        .writeInt(protocolDataUnit.y2())
                        .writeInt(protocolDataUnit.rotationAngle2());

            case JOIN -> {

                encodeString(protocolDataUnit.nickname1(), out);
                encodeString(protocolDataUnit.nickname2(), out);
                encodeString(protocolDataUnit.sessionUUID(), out);
            }
        }
    }
}
