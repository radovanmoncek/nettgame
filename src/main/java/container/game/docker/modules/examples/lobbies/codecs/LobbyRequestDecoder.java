package container.game.docker.modules.examples.lobbies.codecs;

import container.game.docker.modules.examples.lobbies.models.LobbyFlag;
import container.game.docker.modules.examples.lobbies.models.LobbyRequestProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public final class LobbyRequestDecoder extends Decoder<LobbyRequestProtocolDataUnit> {

    public LobbyRequestDecoder(final Map<Byte, Class<? extends ProtocolDataUnit>> protocolIdentifierToProtocolDataUnitBindings) {

        super(protocolIdentifierToProtocolDataUnitBindings, LobbyRequestProtocolDataUnit.class);
    }

    @Override
    public void decodeBodyAfterHeader(final ByteBuf in, final List<? super LobbyRequestProtocolDataUnit> out) {

        final var lobbyFlag = LobbyFlag.values()[in.readByte()];

        switch(lobbyFlag) {

            case JOIN, LEAVE, INFO -> out
                    .add(
                            new LobbyRequestProtocolDataUnit(lobbyFlag, in.readCharSequence(in.readableBytes(), Charset.defaultCharset()).toString())
                    );

            case CREATE -> out.add(new LobbyRequestProtocolDataUnit(lobbyFlag, null));
        }
    }
}
