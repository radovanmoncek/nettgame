package cz.radovanmoncek.test.modules.games.handlers;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.modules.games.handlers.ExampleGameSessionChannelGroupHandler;
import cz.radovanmoncek.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.ship.tables.Character;
import cz.radovanmoncek.ship.tables.GameStatus;
import cz.radovanmoncek.ship.tables.Player;
import cz.radovanmoncek.ship.tables.Position;
import cz.radovanmoncek.ship.tables.Request;
import cz.radovanmoncek.ship.utilities.logging.LoggingUtilities;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
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
        final var name = builder.createString("Test");

        Player.startPlayer(builder);
        Player.addName(builder, name);
        Player.addCharacter(builder, Character.BLUE);
        Player.addPosition(builder, Position.createPosition(builder, 0, 0, 0));

        final var player = Player.endPlayer(builder);

        final var gameStatusRequest = Request.createRequest(builder, player, 0, GameStatus.START_SESSION);

        builder.finish(gameStatusRequest);

        final var encodedGameStatusRequest = ByteBuffer.wrap(builder.sizedByteArray());

        channel.writeInbound(Request.getRootAsRequest(encodedGameStatusRequest));

        TimeUnit.MILLISECONDS.sleep(20);

        channel.advanceTimeBy(20, TimeUnit.SECONDS);

        var gameStateFlatBuffersSerializable = (GameStateFlatBuffersSerializable) channel.readOutbound();

        assertNotNull(gameStateFlatBuffersSerializable);

        var player1Position = (int[][]) ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "playerPositions");
        var name1 = (String[]) ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "playerNames");

        assertNotNull(player1Position);
        assertNotNull(name1);
        assertEquals("Test", name1[0]);
        assertEquals(GameStatus.START_SESSION, ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "gameStatus"));
    }

    void writeGameState() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        final var builder = new FlatBufferBuilder(1024);
        final var position = Position.createPosition(builder, 402, 300, 0);

        Player.startPlayer(builder);
        Player.addPosition(builder, position);

        final var player = Player.endPlayer(builder);
        final var request = Request.createRequest(builder, player, 0, GameStatus.STATE_CHANGE);

        builder.finish(request);

        channel.writeInbound(Request.getRootAsRequest(ByteBuffer.wrap(builder.sizedByteArray())));

        TimeUnit.MILLISECONDS.sleep(20);

        channel.advanceTimeBy(20, TimeUnit.SECONDS);

        var gameStateFlatBuffersSerializable = (GameStateFlatBuffersSerializable) channel.readOutbound();

        assertNotNull(gameStateFlatBuffersSerializable);

        var player1Position = (int[][]) ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "playerPositions");
        var name1 = (String[]) ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "playerNames");

        assertNotNull(player1Position);
        assertNotNull(name1);
        assertEquals("Test", name1[0]);
        assertEquals(GameStatus.STATE_CHANGE, ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "gameStatus"));
    }
        void writeStopGame() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        final var builder = new FlatBufferBuilder(1024);

        builder.finish(Request.createRequest(builder, 0, 0, GameStatus.STOP_SESSION));

        channel.writeInbound(Request.getRootAsRequest(ByteBuffer.wrap(builder.sizedByteArray())));

        //sleeping 2 server ticks, approx. 40 ms
        TimeUnit.MILLISECONDS.sleep(40);

        channel.advanceTimeBy(40, TimeUnit.MILLISECONDS);

        channel.readOutbound();
        final var gameStateFlatBuffersSerializable = channel.readOutbound();

        assertNotNull(gameStateFlatBuffersSerializable);

        assertEquals(GameStatus.STOP_SESSION, ReflectionUtilities.returnValueOnFieldReflectively(gameStateFlatBuffersSerializable, "gameStatus"));
    }
}
