package cz.radovanmoncek.test.modules.games.handlers;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.modules.games.handlers.ExampleGameSessionChannelGroupHandler;
import cz.radovanmoncek.server.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.server.ship.tables.GameStateRequest;
import cz.radovanmoncek.server.ship.tables.GameStatus;
import cz.radovanmoncek.ship.utilities.logging.LoggingUtilities;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExampleGameSessionChannelGroupHandlerTest {
    private EmbeddedChannel channel;

    @BeforeEach
    void setup() {

        (channel = new EmbeddedChannel())
                .pipeline()
                .addLast(new ExampleGameSessionChannelGroupHandler());

        LoggingUtilities.enableGlobalLoggingLevel(Level.ALL);
    }

    @Test
    void playerChannelRead() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        writeStartGame();
        writeGameState();
        writeStopGame();
    }

    void writeStartGame() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        final var builder = new FlatBufferBuilder(1024);

        var name = builder.createString("Test");

        final var gameStatusRequest = GameStateRequest.createGameStateRequest(builder, 0, 0, 0, GameStatus.START_SESSION, 0, name);

        builder.finish(gameStatusRequest);

        final var encodedGameStatusRequest = ByteBuffer.wrap(builder.sizedByteArray());

        channel.writeInbound(GameStateRequest.getRootAsGameStateRequest(encodedGameStatusRequest));

        TimeUnit.MILLISECONDS.sleep(20);

        channel.advanceTimeBy(20, TimeUnit.SECONDS);

        var gameStateFlatBuffersSerializable = (GameStateFlatBuffersSerializable) channel.readOutbound();

        assertNotNull(gameStateFlatBuffersSerializable);

        var player1Position = ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "player1Position");
        var name1 = ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "name1");

        assertNotNull(player1Position);
        assertNotNull(name1);
        assertEquals("Test", name1);
        assertEquals(GameStatus.START_SESSION, ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "gameStatus"));
    }

    void writeGameState() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        final var builder = new FlatBufferBuilder(1024);
        final var gameStatusRequest = GameStateRequest.createGameStateRequest(builder, 408, 300, 0, GameStatus.STATE_CHANGE, 0, 0);

        builder.finish(gameStatusRequest);

        final var encodedGameStatusRequest = ByteBuffer.wrap(builder.sizedByteArray());

        channel.writeInbound(GameStateRequest.getRootAsGameStateRequest(encodedGameStatusRequest));

        TimeUnit.MILLISECONDS.sleep(20);

        channel.advanceTimeBy(20, TimeUnit.SECONDS);

        var gameStateFlatBuffersSerializable = (GameStateFlatBuffersSerializable) channel.readOutbound();

        assertNotNull(gameStateFlatBuffersSerializable);

        var player1Position = ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "player1Position");
        var name1 = ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "name1");

        assertNotNull(player1Position);
        assertNotNull(name1);
        assertEquals("Test", name1);
        assertEquals(GameStatus.STATE_CHANGE, ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "gameStatus"));
    }
        void writeStopGame() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        final var builder = new FlatBufferBuilder(1024);

        builder.finish(GameStateRequest.createGameStateRequest(builder, 0, 0, 0, GameStatus.STOP_SESSION, 0, 0));

        channel.writeInbound(GameStateRequest.getRootAsGameStateRequest(ByteBuffer.wrap(builder.sizedByteArray())));

        //sleeping 2 server ticks, approx. 40 ms
        TimeUnit.MILLISECONDS.sleep(40);

        channel.advanceTimeBy(40, TimeUnit.MILLISECONDS);

        channel.readOutbound();
        final var gameStateFlatBuffersSerializable = channel.readOutbound();

        assertNotNull(gameStateFlatBuffersSerializable);

        assertEquals(GameStatus.STOP_SESSION, ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "gameStatus"));
    }
}
