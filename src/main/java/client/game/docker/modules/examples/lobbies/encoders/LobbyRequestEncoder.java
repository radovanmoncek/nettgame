package client.game.docker.modules.examples.lobbies.encoders;

import container.game.docker.modules.examples.lobby.models.LobbyFlag;
import container.game.docker.modules.examples.lobby.models.LobbyRequestProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public final class LobbyRequestEncoder extends Encoder<LobbyRequestProtocolDataUnit> {

    @Override
    public void encodeBodyAfterHeader(final LobbyRequestProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        out
                .writeByte(LobbyFlag.CREATE.ordinal())
                .writeInt(protocolDataUnit.lobbyHash());
    }
}
