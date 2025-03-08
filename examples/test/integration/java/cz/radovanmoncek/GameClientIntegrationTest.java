package cz.radovanmoncek;

import cz.radovanmoncek.client.modules.games.codecs.GameStateFlatBufferDecoder;
import cz.radovanmoncek.client.modules.games.codecs.GameStateRequestFlatBufferEncoder;
import cz.radovanmoncek.client.modules.games.models.GameStateRequestFlatBuffersSerializable;
import cz.radovanmoncek.client.ship.bootstrap.GameClientBootstrap;
import cz.radovanmoncek.client.ship.builders.GameClientBootstrapBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.ChatMessage;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.client.ship.parents.handlers.ServerChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameClientIntegrationTest {
    private static final Logger logger = LogManager.getLogger(GameClientIntegrationTest.class);
    private static GameClientBootstrap player1, player2;
    private static Method unicastToServerChannel1, unicastToServerChannel2;
    private static ServerChannelHandler<GameState> sessionServerChannelHandler1, sessionServerChannelHandler2;
    private static LinkedBlockingQueue<GameState> gameStateQueue1, gameStateQueue2;
    private static int x1 = 1000, y1 = 1000, x2 = 1000, y2 = 1000;

    @BeforeAll
    static void setup() throws Exception {

        gameStateQueue1 = new LinkedBlockingQueue<>();
        player1 = new GameClientBootstrapBuilder()
                .buildPort(4321)
                .buildServerAddress(InetAddress.getLoopbackAddress())
                .buildLogLevel(LogLevel.INFO)
                .buildChannelHandler(new GameStateFlatBufferDecoder())
                .buildChannelHandler(sessionServerChannelHandler1 = new ServerChannelHandler<GameState>() {

                    public void sendServer(Object message){

                        unicastToServerChannel(message);
                    }

                    @Override
                    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final GameState gameState) {

                        gameStateQueue1.offer(gameState);
                    }

                    {

                        try {

                            unicastToServerChannel1 = getClass()
                                    .getSuperclass()
                                    .getDeclaredMethod("unicastToServerChannel", Object.class);
                        } catch (final NoSuchMethodException noSuchMethodException) {

                            logger.error(noSuchMethodException.getMessage(), noSuchMethodException);
                        }

                        unicastToServerChannel1.setAccessible(true);
                    }
                })
                .buildChannelHandler(new GameStateRequestFlatBufferEncoder())
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'G', GameState.class)
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'g', GameStateRequest.class)
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'm', ChatMessage.class)
                .buildShutdownOnDisconnect(false)
                .build();

        player1.run(1, 9999);
    }

    @AfterEach
    void cleanUpSessionResponseProtocolDataUnitQueue() {

        gameStateQueue1.clear();
    }

    @Test
    @Order(0)
    void nicknameOver8CharactersTest() throws Exception {

        unicastToServerChannel1
                .invoke(
                        sessionServerChannelHandler1,
                        new GameStateRequestFlatBuffersSerializable(0, 0, 0, "VeryLongNicknameThatIsOverEightCharacters", GameStatus.START_SESSION, "")
                );

        final var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(1)
    void emptyPlayerNameTest() throws Exception {

        unicastToServerChannel1
                .invoke(
                        sessionServerChannelHandler1,
                        new GameStateRequestFlatBuffersSerializable(0, 0, 0, "", GameStatus.START_SESSION, "")
                );

        final var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(2)
    void sessionStartTest() throws Exception {

        final var sessionRequestProtocolDataUnit = new GameStateRequestFlatBuffersSerializable(0, 0, 0, "Test", GameStatus.START_SESSION, "");

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, sessionRequestProtocolDataUnit);

        final var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals("Test", gameState.player1().name());
        assertEquals(GameStatus.START_SESSION, gameState.game().status());
        assertEquals(1000, gameState.player1().x());
        assertEquals(1000, gameState.player1().y());
        assertEquals(0, gameState.player1().rotationAngle());
    }

    @AfterAll
    static void tearDown() {

        player1.shutdownGracefullyAfterNSeconds(0);
    }

    @Test
    @Order(3)
    void sessionValidStateWithinBoundsTest() throws InterruptedException, IllegalAccessException, InvocationTargetException {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, new GameStateRequestFlatBuffersSerializable(1008, 1008, 90, "", GameStatus.STATE_CHANGE, ""));

        final var gameState = gameStateQueue1.take();

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

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, new GameStateRequestFlatBuffersSerializable(801, 601, 15, "", GameStatus.STATE_CHANGE, ""));

        final var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(5)
    void sessionInvalidStateUnderBoundsTest() throws Exception {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, new GameStateRequestFlatBuffersSerializable(-1, -1, -15, "", GameStatus.STATE_CHANGE, ""));

        final var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(6)
    void sessionValidStateMoveDeltaTest() throws Exception {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, new GameStateRequestFlatBuffersSerializable(1000, 1008, 180, "", GameStatus.STATE_CHANGE, ""));

        var gameState = gameStateQueue1.take();

        assertNotNull(gameState);
        assertNotNull(gameState.player1());

        assertEquals(GameStatus.STATE_CHANGE, gameState.game().status());
        assertEquals(1000, gameState.player1().x());
        assertEquals(1008, gameState.player1().y());
        assertEquals(180, gameState.player1().rotationAngle());

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, new GameStateRequestFlatBuffersSerializable(1008, 1008, 270, "", GameStatus.STATE_CHANGE, ""));

        gameState = gameStateQueue1.take();

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

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, new GameStateRequestFlatBuffersSerializable(2, 6, 45, "", GameStatus.STATE_CHANGE, ""));

        var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, new GameStateRequestFlatBuffersSerializable(4, 4, 45, "", GameStatus.STATE_CHANGE, ""));

        gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    @Test
    @Order(8)
    void sessionEndTest() throws Exception {

        unicastToServerChannel1.invoke(
                sessionServerChannelHandler1,
                new GameStateRequestFlatBuffersSerializable(0, 0, 0, "", GameStatus.STOP_SESSION, "")
        );

        final var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.STOP_SESSION, gameState.game().status());
    }

    @Test
    @Order(9)
    void joinSessionTest() throws Exception {

        final var instanceField = GameClientBootstrap.class.getDeclaredField("instance");

        instanceField.setAccessible(true);
        instanceField.set(player1, null);

        gameStateQueue2 = new LinkedBlockingQueue<>();

        player2 = new GameClientBootstrapBuilder()
                .buildLogLevel(LogLevel.INFO)
                .buildChannelHandler(new GameStateFlatBufferDecoder())
                .buildChannelHandler(sessionServerChannelHandler2 = new ServerChannelHandler<GameState>() {

                    {

                        try {
                            (
                                    unicastToServerChannel2 =
                                            getClass()
                                                    .getSuperclass()
                                                    .getDeclaredMethod("unicastToServerChannel", Object.class)
                            )
                                    .setAccessible(true);
                        } catch (final NoSuchMethodException noSuchMethodException) {

                            logger.error(noSuchMethodException.getMessage(), noSuchMethodException);
                        }
                    }

                    public void sendServer(Object message) {

                        unicastToServerChannel(message);
                    }

                    @Override
                    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final GameState protocolDataUnit) {

                        gameStateQueue2.offer(protocolDataUnit);
                    }
                })
                .buildChannelHandler(new GameStateRequestFlatBufferEncoder())
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'G', GameState.class)
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'g', GameStateRequest.class)
                .build();

        player2.run(1, 9999);

        unicastToServerChannel1.invoke(
                sessionServerChannelHandler1,
                new GameStateRequestFlatBuffersSerializable(0, 0, 0, "Test", GameStatus.START_SESSION, "")
        );

        var gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.START_SESSION, gameState.game().status());

        final var gameCode = gameState.game().gameCode();

        assertNotNull(gameCode);

        /*gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.STATE_CHANGE, gameState.game().status());
*/

        unicastToServerChannel2.invoke(
                sessionServerChannelHandler2,
                new GameStateRequestFlatBuffersSerializable(0, 0, 0, "Test2", GameStatus.JOIN_SESSION, gameCode)
        );

        var gameStateSecondPlayer = gameStateQueue2.take();

        assertNotNull(gameStateSecondPlayer);

        assertEquals(GameStatus.JOIN_SESSION, gameStateSecondPlayer.game().status());

        assertNotNull(gameStateSecondPlayer.player1());
        assertNotNull(gameStateSecondPlayer.player2());
        assertNotNull(gameStateSecondPlayer.player1().name());
        assertNotNull(gameStateSecondPlayer.player2().name());

        gameState = gameStateQueue1.take();

        assertNotNull(gameState);

        assertEquals(GameStatus.JOIN_SESSION, gameState.game().status());
        assertEquals("Test", gameState.player1().name());
        assertEquals("Test2", gameState.player2().name());

        //gameState = gameStateQueue1.take();
        //gameStateSecondPlayer = gameStateQueue2.take();

        //assertNotNull(gameState);
        assertNotNull(gameStateSecondPlayer);

        //assertEquals(GameStatus.STATE_CHANGE, gameState.game().status());
        //assertEquals(GameStatus.STATE_CHANGE, gameStateSecondPlayer.game().status());
        assertEquals(GameStatus.JOIN_SESSION, gameStateSecondPlayer.game().status());
        assertEquals("Test2", gameStateSecondPlayer.player1().name());
        assertEquals("Test", gameStateSecondPlayer.player2().name());
    }

    @RepeatedTest(100)
    @Order(10)
    void twoPlayerSessionTest() throws Exception {

        unicastToServerChannel1.invoke(
                sessionServerChannelHandler1,
                new GameStateRequestFlatBuffersSerializable(x1 + 8, y1, 90, "", GameStatus.STATE_CHANGE, "")
        );

        var gameStateFirstPlayer = gameStateQueue1.take();

        assertNotNull(gameStateFirstPlayer);

        assertEquals(GameStatus.STATE_CHANGE, gameStateFirstPlayer.game().status());

        var gameStateSecondPlayer = gameStateQueue2.take();

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

        unicastToServerChannel2.invoke(
                sessionServerChannelHandler2,
                new GameStateRequestFlatBuffersSerializable(x2, y2 + 8, 90, "", GameStatus.STATE_CHANGE, "")
        );

        gameStateSecondPlayer = gameStateQueue2.take();

        assertNotNull(gameStateSecondPlayer);

        assertEquals(GameStatus.STATE_CHANGE, gameStateSecondPlayer.game().status());

        gameStateFirstPlayer = gameStateQueue1.take();

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

        player2.shutdownGracefully();
    }
}
