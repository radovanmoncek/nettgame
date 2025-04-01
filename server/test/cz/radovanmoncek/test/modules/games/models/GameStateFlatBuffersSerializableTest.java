package cz.radovanmoncek.test.modules.games.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.modules.games.models.GameStateFlatBuffersSerializable;
import cz.radovanmoncek.ship.tables.Character;
import cz.radovanmoncek.ship.tables.GameState;
import cz.radovanmoncek.ship.tables.GameStatus;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateFlatBuffersSerializableTest {

    @Test
    void serialize() {

        final var serializable = new GameStateFlatBuffersSerializable()
                .withPlayer(
                        new int[]{5, 5, 45},
                        "Test",
                        Character.BLUE
                )
                .withPlayer(
                        new int[]{5, 10, 45},
                        "Test2",
                        Character.RED
                )
                .withGameState(0, GameStatus.STATE_CHANGE, "lll");
        final var result = serializable.serialize(new FlatBufferBuilder(1024));

        assertNotNull(result);

        assertNotEquals(0, result.length);

        final var gameState = GameState.getRootAsGameState(ByteBuffer.wrap(result));

        assertNotNull(gameState);

        assertEquals(2, gameState.playersLength());
        assertThrows(IndexOutOfBoundsException.class, () -> gameState.players(2));
        assertEquals("lll", gameState.gameMetadata().gameCode());
        assertEquals("Test", gameState.players(0).name());
        assertEquals(5, gameState.players(0).position().x());
        assertEquals(5, gameState.players(0).position().y());
        assertEquals(45, gameState.players(0).position().rotation());
        assertEquals("Test2", gameState.players(1).name());
        assertEquals(5, gameState.players(1).position().x());
        assertEquals(10, gameState.players(1).position().y());
        assertEquals(45, gameState.players(1).position().rotation());
    }
}
