package client.game.docker.modules.examples.lobbies.codecs;

import container.game.docker.modules.examples.lobbies.models.LobbyFlag;
import container.game.docker.modules.examples.lobbies.models.LobbyRequestProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.Map;

public final class LobbyRequestEncoder extends Encoder<LobbyRequestProtocolDataUnit> {

    public LobbyRequestEncoder(final Map<Class<? extends ProtocolDataUnit>, Byte> protocolDataUnitToProtocolIdentifierBindings) {

        super(protocolDataUnitToProtocolIdentifierBindings);
    }

    @Override
    public void encodeBodyAfterHeader(final LobbyRequestProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        out
                .writeByte(LobbyFlag.CREATE.ordinal())
                .writeCharSequence(protocolDataUnit.lobbyUUID(), Charset.defaultCharset());
    }
}
