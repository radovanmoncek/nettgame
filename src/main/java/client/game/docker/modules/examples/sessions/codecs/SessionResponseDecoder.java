package client.game.docker.modules.examples.sessions.codecs;

import container.game.docker.modules.examples.sessions.models.SessionFlag;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

public class SessionResponseDecoder extends Decoder<SessionResponseProtocolDataUnit> {

    public SessionResponseDecoder() {

        super(SessionResponseProtocolDataUnit.class);
    }

    @Override
    protected void decodeBodyAfterHeader(final ByteBuf in, final List<? super SessionResponseProtocolDataUnit> out) {

        final var sessionFlag = SessionFlag.values()[in.readByte()];

        switch (sessionFlag) {

            case START -> out.add(
                    SessionResponseProtocolDataUnit.newSTART(
                            decodeString(in),
                            decodeString(in)
                    )
            );

            case STOP -> out.add(SessionResponseProtocolDataUnit.newSTOP());

            case STATE -> out.add(
                    SessionResponseProtocolDataUnit.newSTATE(
                            in.readInt(),
                            in.readInt(),
                            in.readInt(),
                            in.readInt(),
                            in.readInt(),
                            in.readInt()
                    )
            );

            case INVALID -> out.add(SessionResponseProtocolDataUnit.newINVALID());

            case JOIN -> out.add(
                    SessionResponseProtocolDataUnit.newJOIN(
                            decodeString(in),
                            decodeString(in)
                    )
            );
        }
    }
}
