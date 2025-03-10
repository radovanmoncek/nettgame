package cz.radovanmoncek;

import cz.radovanmoncek.client.modules.games.models.GameStateRequestFlatBuffersSerializable;
import cz.radovanmoncek.client.ship.bootstrap.GameClientBootstrap;
import cz.radovanmoncek.client.ship.builders.GameClientBootstrapBuilder;
import cz.radovanmoncek.client.ship.directors.GameClientBootstrapDirector;
import cz.radovanmoncek.client.ship.parents.handlers.ServerChannelHandler;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameClientIntegrationTest {
    private static final Logger logger = LogManager.getLogger(GameClientIntegrationTest.class);
    private static GameClientBootstrap player1, player2;
    private static TestingGameStateServerChannelHandler gameStateTestingGameStateServerChannelHandler1, gameStateTestingGameStateServerChannelHandler2;
    private static int x1 = 1000, y1 = 1000, x2 = 1000, y2 = 1000;

    @BeforeAll
    static void setup() {

        gameStateTestingGameStateServerChannelHandler1 = new TestingGameStateServerChannelHandler();

        player1 = new GameClientBootstrapDirector(new GameClientBootstrapBuilder())
                .makeDefaultGameClientBootstrapBuilder()
                .buildChannelHandler(gameStateTestingGameStateServerChannelHandler1)
                .build();
        player1.run();
    }

    @Test
    @Order(0)
    void nicknameOver8CharactersTest() throws InterruptedException {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(0, 0, 0, "VeryLongNicknameThatIsOverEightCharacters", GameStatus.START_SESSION, ""));

        var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(1)
    void emptyPlayerNameTest() throws InterruptedException {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(0, 0, 0, "", GameStatus.START_SESSION, ""));

        final var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(2)
    void sessionStartTest() throws InterruptedException {

        final var sessionRequestProtocolDataUnit = new GameStateRequestFlatBuffersSerializable(0, 0, 0, "Test", GameStatus.START_SESSION, "");

        gameStateTestingGameStateServerChannelHandler1.unicast(sessionRequestProtocolDataUnit);

        final var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals("Test", gameState.player1().name());
        assertEquals(GameStatus.START_SESSION, gameState.game().status());
        assertEquals(1000, gameState.player1().x());
        assertEquals(1000, gameState.player1().y());
        assertEquals(0, gameState.player1().rotationAngle());
    }

    @AfterAll
    static void tearDown() {

        gameStateTestingGameStateServerChannelHandler1.disconnect();
    }

    @Test
    @Order(3)
    void sessionValidStateWithinBoundsTest() throws InterruptedException {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(1008, 1008, 90, "", GameStatus.STATE_CHANGE, ""));

        final var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);
        assertNotNull(gameState.game());

        assertEquals(GameStatus.STATE_CHANGE, gameState.game().status());

        assertNotNull(gameState.player1());

        assertEquals(1008, gameState.player1().x());
        assertEquals(1008, gameState.player1().y());
        assertEquals(90, gameState.player1().rotationAngle());
    }

    @Test
    @Order(4)
    void sessionInvalidStateOverBoundsTest() throws Exception {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(801, 601, 15, "", GameStatus.STATE_CHANGE, ""));

        final var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(5)
    void sessionInvalidStateUnderBoundsTest() throws Exception {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(-1, -1, -15, "", GameStatus.STATE_CHANGE, ""));

        final var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(6)
    void sessionValidStateMoveDeltaTest() throws Exception {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(1000, 1008, 180, "", GameStatus.STATE_CHANGE, ""));

        var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);
        assertNotNull(gameState.player1());

        assertEquals(GameStatus.STATE_CHANGE, gameState.game().status());
        assertEquals(1000, gameState.player1().x());
        assertEquals(1008, gameState.player1().y());
        assertEquals(180, gameState.player1().rotationAngle());

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(1008, 1008, 270, "", GameStatus.STATE_CHANGE, ""));

        gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);
        assertNotNull(gameState.player1());

        assertEquals(GameStatus.STATE_CHANGE, gameState.game().status());
        assertEquals(1008, gameState.player1().x());
        assertEquals(1008, gameState.player1().y());
        assertEquals(270, gameState.player1().rotationAngle());
    }

    @Test
    @Order(7)
    void sessionInvalidStateMoveDeltaTest() throws Exception {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(2, 6, 45, "", GameStatus.STATE_CHANGE, ""));

        var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(4, 4, 45, "", GameStatus.STATE_CHANGE, ""));

        gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(8)
    void sessionEndTest() throws Exception {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(0, 0, 0, "", GameStatus.STOP_SESSION, ""));

        final var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.STOP_SESSION, gameState.game().status());
    }

    @Test
    @Order(9)
    void joinSessionTest() throws Exception {

        final var instanceField = GameClientBootstrap.class.getDeclaredField("instance");

        instanceField.setAccessible(true);
        instanceField.set(player1, null);

        gameStateTestingGameStateServerChannelHandler2 = new TestingGameStateServerChannelHandler();

        player2 = new GameClientBootstrapDirector(new GameClientBootstrapBuilder())
                .makeDefaultGameClientBootstrapBuilder()
                .buildChannelHandler(gameStateTestingGameStateServerChannelHandler2)
                .build();
        player2.run();

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(0, 0, 0, "Test", GameStatus.START_SESSION, ""));

        var gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.START_SESSION, gameState.game().status());

        final var gameCode = gameState
                .game()
                .gameCode();

        assertNotNull(gameCode);

        TimeUnit.MILLISECONDS.sleep(2000);

        gameStateTestingGameStateServerChannelHandler2.unicast(new GameStateRequestFlatBuffersSerializable(0, 0, 0, "Test2", GameStatus.JOIN_SESSION, gameCode));

        var gameStateSecondPlayer = gameStateTestingGameStateServerChannelHandler2.poll();

        assertNotNull(gameStateSecondPlayer);

        assertEquals(GameStatus.JOIN_SESSION, gameStateSecondPlayer.game().status());

        assertNotNull(gameStateSecondPlayer.player1());
        assertNotNull(gameStateSecondPlayer.player2());
        assertNotNull(gameStateSecondPlayer.player1().name());
        assertNotNull(gameStateSecondPlayer.player2().name());

        gameState = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.JOIN_SESSION, gameState.game().status());
        assertEquals("Test", gameState.player1().name());
        assertEquals("Test2", gameState.player2().name());

        assertNotNull(gameStateSecondPlayer);

        assertEquals(GameStatus.JOIN_SESSION, gameStateSecondPlayer.game().status());
        assertEquals("Test2", gameStateSecondPlayer.player1().name());
        assertEquals("Test", gameStateSecondPlayer.player2().name());
    }

    @RepeatedTest(100)
    @Order(10)
    void twoPlayerSessionTest() throws Exception {

        gameStateTestingGameStateServerChannelHandler1.unicast(new GameStateRequestFlatBuffersSerializable(x1 + 8, y1, 90, "", GameStatus.STATE_CHANGE, ""));

        var gameStateFirstPlayer = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameStateFirstPlayer);

        assertEquals(GameStatus.STATE_CHANGE, gameStateFirstPlayer.game().status());

        var gameStateSecondPlayer = gameStateTestingGameStateServerChannelHandler2.poll();

        assertNotNull(gameStateSecondPlayer);

        assertEquals(GameStatus.STATE_CHANGE, gameStateSecondPlayer.game().status());
        assertEquals(x1 + 8, gameStateFirstPlayer.player1().x());
        assertEquals(y1, gameStateFirstPlayer.player1().y());
        assertEquals(90, gameStateFirstPlayer.player1().rotationAngle());
        assertEquals(x1 + 8, gameStateSecondPlayer.player2().x());
        assertEquals(y1, gameStateSecondPlayer.player2().y());
        assertEquals(90, gameStateSecondPlayer.player2().rotationAngle());

        x1 = gameStateFirstPlayer.player1().x();
        y1 = gameStateFirstPlayer.player1().y();

        gameStateTestingGameStateServerChannelHandler2.unicast(new GameStateRequestFlatBuffersSerializable(x2, y2 + 8, 90, "", GameStatus.STATE_CHANGE, ""));

        gameStateSecondPlayer = gameStateTestingGameStateServerChannelHandler2.poll();

        assertNotNull(gameStateSecondPlayer);

        assertEquals(GameStatus.STATE_CHANGE, gameStateSecondPlayer.game().status());

        gameStateFirstPlayer = gameStateTestingGameStateServerChannelHandler1.poll();

        assertNotNull(gameStateFirstPlayer);

        assertEquals(GameStatus.STATE_CHANGE, gameStateFirstPlayer.game().status());
        assertEquals(x2, gameStateFirstPlayer.player2().x());
        assertEquals(y2 + 8, gameStateFirstPlayer.player2().y());
        assertEquals(90, gameStateFirstPlayer.player2().rotationAngle());
        assertEquals(x2, gameStateSecondPlayer.player1().x());
        assertEquals(y2 + 8, gameStateSecondPlayer.player1().y());
        assertEquals(90, gameStateSecondPlayer.player1().rotationAngle());

        x2 = gameStateSecondPlayer.player1().x();
        y2 = gameStateSecondPlayer.player1().y();
    }

    @Test
    @Order(11)
    void shutdownJoinedPlayerGameClientTest() {

        gameStateTestingGameStateServerChannelHandler2.disconnect();
    }

    /**
     * Inspired by {@link io.netty.channel.embedded.EmbeddedChannel}
     */
    public static class TestingGameStateServerChannelHandler extends ServerChannelHandler<GameState> {
        private final LinkedBlockingQueue<GameState> queue = new LinkedBlockingQueue<>();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, GameState msg) {

            queue.offer(msg);
        }

        @Override
        public void unicast(Object msg) {

            super.unicast(msg);
        }

        public GameState poll() throws InterruptedException {

            return queue.poll(100, TimeUnit.MILLISECONDS);
        }

        public void disconnect() {

            queue.clear();

            super.disconnect();
        }
    }
}
