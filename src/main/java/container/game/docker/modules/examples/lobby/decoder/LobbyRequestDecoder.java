package container.game.docker.modules.examples.lobby.decoder;

import container.game.docker.modules.examples.lobby.models.LobbyFlag;
import container.game.docker.modules.examples.lobby.models.LobbyRequestProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public final class LobbyRequestDecoder extends Decoder<LobbyRequestProtocolDataUnit> {

    @Override
    public void decodeBodyAfterHeader(final ByteBuf in, final List<? super LobbyRequestProtocolDataUnit> out) {

        final var lobbyFlag = LobbyFlag.values()[in.readByte()];

        switch(lobbyFlag) {

            case JOIN, LEAVE, INFO -> out.add(new LobbyRequestProtocolDataUnit(lobbyFlag, in.readInt()));

            case CREATE -> out.add(new LobbyRequestProtocolDataUnit(lobbyFlag, null));
        }
    }

    @Override
    protected int supplyProtocolIdentifier() {

        return new LobbyRequestProtocolDataUnit(null, null)
                .getProtocolIdentifier();
    }
}
