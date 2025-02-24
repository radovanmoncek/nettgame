package container.game.docker.modules.examples.lobbies.codecs;

import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public final class LobbyResponseEncoder extends Encoder<LobbyResponseProtocolDataUnit> {

    @Override
    public void encodeBodyAfterHeader(final LobbyResponseProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        out.writeByte(protocolDataUnit.lobbyFlag().ordinal());

        encodeString(protocolDataUnit.memberNickname1(), out);
        encodeString(protocolDataUnit.memberNickname2(), out);
        encodeString(protocolDataUnit.lobbyUUID(), out);
    }
}
