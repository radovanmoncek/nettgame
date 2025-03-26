package cz.radovanmoncek.test.modules.games.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.server.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.server.ship.tables.GameState;
import cz.radovanmoncek.server.ship.tables.GameStatus;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateFlatBuffersSerializableTest {

    @Test
    void serialize() {

        final var serializable = new GameStateFlatBuffersSerializable()
                .withGameCode("lll")
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withName1("Test")
                .withPlayer1Position(new int[]{5, 5, 45});
        final var result = serializable.serialize(new FlatBufferBuilder(1024));

        assertNotNull(result);

        assertNotEquals(0, result.length);

        final var gameState = GameState.getRootAsGameState(ByteBuffer.wrap(result));

        assertNotNull(gameState);

        assertEquals("lll", gameState.game().gameCode());
        assertEquals("Test", gameState.player1().name());
        assertEquals(5, gameState.player1().x());
        assertEquals(5, gameState.player1().y());
        assertEquals(45, gameState.player1().rotationAngle());
    }
}
