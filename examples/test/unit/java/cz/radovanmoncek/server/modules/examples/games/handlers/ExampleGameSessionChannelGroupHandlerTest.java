package cz.radovanmoncek.server.modules.examples.games.handlers;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.modules.games.handlers.ExampleGameSessionChannelGroupHandler;
import cz.radovanmoncek.server.modules.games.models.GameStateFlatBufferSerializable;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExampleGameSessionChannelGroupHandlerTest {
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {

        channel = new EmbeddedChannel();

        channel
                .pipeline()
                .addLast(new ExampleGameSessionChannelGroupHandler());
    }

    @Test
    void playerChannelRead() throws InterruptedException {

        final var builder = new FlatBufferBuilder(1024);
        final var name = builder.createString("Test");
        final var gameStatusRequest = GameStateRequest.createGameStateRequest(builder, 0, 0, 0, GameStatus.START_SESSION, 0, name);

        builder.finish(gameStatusRequest);

        final var encodedGameStatusRequest = ByteBuffer.wrap(builder.sizedByteArray());

        channel.writeInbound(GameStateRequest.getRootAsGameStateRequest(encodedGameStatusRequest));

        TimeUnit.MILLISECONDS.sleep(10);

        final var gameStateFlatBuffersSerializable = (GameStateFlatBufferSerializable) channel.readOutbound();

        assertNotNull(gameStateFlatBuffersSerializable);
        assertNotNull(gameStateFlatBuffersSerializable.players()[0]);

        assertEquals(GameStatus.START_SESSION, gameStateFlatBuffersSerializable.game().gameStatus());
        assertEquals("Test", gameStateFlatBuffersSerializable.players()[0].name());
    }

    @Test
    void playerDisconnected() {
    }
}