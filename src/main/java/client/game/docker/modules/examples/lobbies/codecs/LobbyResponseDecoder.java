package client.game.docker.modules.examples.lobbies.codecs;

import container.game.docker.modules.examples.lobbies.models.LobbyFlag;
import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

public class LobbyResponseDecoder extends Decoder<LobbyResponseProtocolDataUnit> {

    public LobbyResponseDecoder() {

        super(LobbyResponseProtocolDataUnit.class);
    }

    @Override
    public void decodeBodyAfterHeader(final ByteBuf in, final List<? super LobbyResponseProtocolDataUnit> out) {

        final var lobbyFlag = LobbyFlag.values()[in.readByte()];

        final var lobbyResponseProtocolDataUnit = new LobbyResponseProtocolDataUnit(
                lobbyFlag,
                decodeString(in),
                decodeString(in),
                decodeString(in)
        );

        out.add(lobbyResponseProtocolDataUnit);
    }
}
