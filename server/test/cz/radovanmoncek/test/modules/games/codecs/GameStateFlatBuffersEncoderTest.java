package cz.radovanmoncek.test.modules.games.codecs;

import cz.radovanmoncek.modules.games.codecs.GameStateFlatBuffersEncoder;
import cz.radovanmoncek.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.ship.tables.GameStatus;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameStateFlatBuffersEncoderTest {
    private static EmbeddedChannel channel;

    @BeforeEach
    void setUp() {

        (channel = new EmbeddedChannel())
                .pipeline()
                .addLast(new GameStateFlatBuffersEncoder());
    }

    @Test
    void encodeHeader() {

        final var gameStateResponse = new GameStateFlatBuffersSerializable()
                .withGameState(0, GameStatus.JOIN_SESSION, "code");
    }

    @Test
    void encodeBodyAfterHeader() {
    }
}