package container.game.docker.modules.examples.sessions.codes;

import container.game.docker.modules.examples.sessions.models.SessionFlag;
import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;

import java.util.List;
import java.util.Map;

public final class SessionRequestDecoder extends Decoder<SessionRequestProtocolDataUnit> {

    public SessionRequestDecoder(final Map<Byte, Class<? extends ProtocolDataUnit>> protocolIdentifierToProtocolDataUnitBindings) {

        super(protocolIdentifierToProtocolDataUnitBindings, SessionRequestProtocolDataUnit.class);
    }

    @Override
    protected void decodeBodyAfterHeader(final ByteBuf in, final List<? super SessionRequestProtocolDataUnit> out) {

        final var sessionFlag = SessionFlag.values()[in.readByte()];

        switch (sessionFlag) {

            case START -> out.add(SessionRequestProtocolDataUnit.newSTART(decodeString(in)));

            case STATE -> out.add(SessionRequestProtocolDataUnit.newSTATE(in.readInt(), in.readInt(), in.readInt()));

            case STOP -> out.add(SessionRequestProtocolDataUnit.newSTOP());

            case JOIN -> out.add(
                    SessionRequestProtocolDataUnit.newJOIN(
                            decodeString(in),
                            decodeString(in)
                    )
            );
        }
    }
}
