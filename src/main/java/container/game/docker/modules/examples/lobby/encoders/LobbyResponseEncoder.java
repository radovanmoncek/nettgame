package container.game.docker.modules.examples.lobby.encoders;

import container.game.docker.modules.examples.lobby.models.LobbyResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public final class LobbyResponseEncoder extends Encoder<LobbyResponseProtocolDataUnit> {

    @Override
    public void encodeBodyAfterHeader(final LobbyResponseProtocolDataUnit protocolDataUnit, final ByteBuf out) {

        out
                .writeByte(protocolDataUnit.lobbyFlag().ordinal())
                .writeLong(protocolDataUnit.lobbyHash())
                .writeBytes(
                        ByteBufUtil
                                .encodeString(
                                out.alloc(),
                                CharBuffer.wrap(protocolDataUnit.memberNickname1()),
                                Charset.defaultCharset()
                        )
                )
                .writeBytes(
                        ByteBufUtil
                                .encodeString(
                                out.alloc(),
                                CharBuffer.wrap(protocolDataUnit.memberNickname2()),
                                Charset.defaultCharset()
                        )
                );
    }
}
