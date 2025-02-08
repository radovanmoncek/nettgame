package container.game.docker.modules.examples.session.decoders;

import container.game.docker.modules.examples.session.models.SessionFlag;
import container.game.docker.ship.parents.codecs.Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import container.game.docker.modules.examples.session.models.SessionRequestProtocolDataUnit;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

public final class SessionRequestDecoder extends Decoder<SessionRequestProtocolDataUnit> {

    @Override
    protected void decodeBodyAfterHeader(final ByteBuf in, final List<? super SessionRequestProtocolDataUnit> out) {

        final var sessionFlag = SessionFlag.values()[in.readByte()];

        switch (sessionFlag) {

            case START -> {

                final var username = in.toString(in.readerIndex() + 4 * Integer.BYTES, MAX_NICKNAME_LENGTH, Charset.defaultCharset()).trim();

                in.readerIndex(in.readerIndex() + MAX_NICKNAME_LENGTH);

                out.add(new SessionRequestProtocolDataUnit(sessionFlag, null, null, null, null, username));
            }

            case STATE -> out.add(new SessionRequestProtocolDataUnit(sessionFlag, in.readInt(), in.readInt(), in.readInt(), in.readInt(), null));

            case STOP -> out.add(new SessionRequestProtocolDataUnit(sessionFlag, null, null, null, null, null));

            case JOIN -> {

                final var sessionHash = in.readInt();

                final var username = in.toString(in.readerIndex() + 4 * Integer.BYTES, MAX_NICKNAME_LENGTH, Charset.defaultCharset()).trim();

                in.readerIndex(in.readerIndex() + MAX_NICKNAME_LENGTH);

                out.add(new SessionRequestProtocolDataUnit(sessionFlag, sessionHash, null, null, null, username));
            }
        }
    }

    @Override
    protected int supplyProtocolIdentifier() {

        return new SessionRequestProtocolDataUnit(null, null, null, null, null, null)
                .getProtocolIdentifier();
    }
}
