package client.game.docker.modules.examples.sessions.decoders;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import container.game.docker.modules.examples.session.models.SessionFlag;
import container.game.docker.modules.examples.session.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.parents.codecs.Decoder;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.List;

import static container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler.MAX_NICKNAME_LENGTH;

public class SessionResponseDecoder extends Decoder<SessionResponseProtocolDataUnit> {

    @Override
    protected void decodeBodyAfterHeader(final ByteBuf in, final List<? super SessionResponseProtocolDataUnit> out) {

        final var sessionFlag = SessionFlag.values()[in.readByte()];

        switch (sessionFlag) {

            case START -> out.add(
                    new SessionResponseProtocolDataUnit(
                            sessionFlag,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            in.toString(in.readerIndex(6 * Integer.BYTES).readerIndex(), MAX_NICKNAME_LENGTH, Charset.defaultCharset()),
                            null
                    )
            );
        }
    }

    @Override
    protected int supplyProtocolIdentifier() {

        return new SessionResponseProtocolDataUnit(null, null, null, null, null, null, null, null, null, null)
                .getProtocolIdentifier();
    }
}
