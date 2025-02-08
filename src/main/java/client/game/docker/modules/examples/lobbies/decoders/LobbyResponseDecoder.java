package client.game.docker.modules.examples.lobbies.decoders;

import container.game.docker.modules.examples.lobby.models.LobbyFlag;
import container.game.docker.modules.examples.lobby.models.LobbyResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.List;

import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

public class LobbyResponseDecoder extends Decoder<LobbyResponseProtocolDataUnit> {

    @Override
    public void decodeBodyAfterHeader(final ByteBuf in, final List<? super LobbyResponseProtocolDataUnit> out) {

        final var lobbyFlag = LobbyFlag.values()[in.readByte()];

        final var lobbyResponseProtocolDataUnit = new LobbyResponseProtocolDataUnit(
                lobbyFlag,
                in.readInt(),
                in.toString(in.readerIndex(), MAX_NICKNAME_LENGTH, Charset.defaultCharset()).trim(),
                in.toString(in.readerIndex(), MAX_NICKNAME_LENGTH, Charset.defaultCharset()).trim()
        );

        out.add(lobbyResponseProtocolDataUnit);
    }

    @Override
    protected int supplyProtocolIdentifier() {

        return new LobbyResponseProtocolDataUnit(null, null, null, null)
                .getProtocolIdentifier();
    }
}
