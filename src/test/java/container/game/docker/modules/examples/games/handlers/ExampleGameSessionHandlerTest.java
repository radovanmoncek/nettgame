package container.game.docker.modules.examples.games.handlers;

import client.game.docker.modules.examples.sessions.codecs.GameStateFlatBufferDecoder;
import client.game.docker.modules.examples.sessions.codecs.GameStateRequestFlatBufferEncoder;
import client.game.docker.modules.examples.sessions.models.GameStateRequestFlatBuffersSerializable;
import client.game.docker.ship.bootstrap.GameClientBootstrap;
import client.game.docker.ship.builders.GameClientBootstrapBuilder;
import client.game.docker.ship.parents.handlers.ServerChannelHandler;
import container.game.docker.ship.bootstrap.examples.SampleInstanceContainer;
import container.game.docker.ship.examples.compiled.schemas.ChatMessage;
import container.game.docker.ship.examples.compiled.schemas.GameState;
import container.game.docker.ship.examples.compiled.schemas.GameStateRequest;
import container.game.docker.ship.examples.compiled.schemas.GameStatus;
import io.netty.handler.logging.LogLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExampleGameSessionHandlerTest {
    private static final Logger logger = LogManager.getLogger(ExampleGameSessionHandlerTest.class);
    private static GameClientBootstrap player1, player2;
    private static Method unicastToServerChannel1, unicastToServerChannel2;
    private static ServerChannelHandler<GameState> sessionServerChannelHandler1, sessionServerChannelHandler2;
    private static Queue<GameState> sessionResponseProtocolDataUnitQueue1, sessionResponseProtocolDataUnitQueue2;
    private static int x1 = 1000, y1 = 1000, x2 = 1000, y2 = 1000;

    @BeforeAll
    static void setup() throws Exception {

        SampleInstanceContainer.main(null);

        sessionResponseProtocolDataUnitQueue1 = new LinkedList<>();

        new GameClientBootstrapBuilder()
                .buildPort(4321)
                .buildServerAddress(InetAddress.getLoopbackAddress())
                .buildLogLevel(LogLevel.INFO)
                .buildChannelHandler(new GameStateFlatBufferDecoder())
                .buildChannelHandler(sessionServerChannelHandler1 = new ServerChannelHandler<>() {

                    @Override
                    protected void serverChannelRead(final GameState gameState) {

                        sessionResponseProtocolDataUnitQueue1.offer(gameState);
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
                .build()
                .run(1, 9999);
    }

    @AfterEach
    void cleanUpSessionResponseProtocolDataUnitQueue() {

        sessionResponseProtocolDataUnitQueue1.clear();
    }

    @Test
    @Order(1)
    void nicknameOver8CharactersTest() throws Exception {

        unicastToServerChannel1
                .invoke(
                        sessionServerChannelHandler1,
                        new GameStateRequestFlatBuffersSerializable(0, 0, 0, "VeryLongNicknameThatIsOverEightCharacters", GameStatus.START_SESSION, null)
                );

        blockUntilSessionResponseReceived();

        final var gameState = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.game().status());
    }

    /*@Test
    @Order(2)
    void sessionStartTest() throws Exception {

        final var sessionRequestProtocolDataUnit = GameStateFlatBufferSerializable.newSTART("Test");

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, sessionRequestProtocolDataUnit);

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.START, sessionResponseProtocolDataUnit.sessionFlag());

        assertNotNull(sessionResponseProtocolDataUnit.sessionUUID());

        assertEquals("Test", sessionResponseProtocolDataUnit.nickname1());

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(1000, sessionResponseProtocolDataUnit.x1());
        assertEquals(1000, sessionResponseProtocolDataUnit.y1());
        assertEquals(0, sessionResponseProtocolDataUnit.rotationAngle1());
    }*/

    @AfterAll
    static void tearDown() {

        player1.shutdownGracefullyAfterNSeconds(0);
    }

    /*@Test
    @Order(3)
    void sessionValidStateWithinBoundsTest() throws Exception {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, GameStateFlatBufferSerializable.newSTATE(1008, 1008, 90));

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(1008, sessionResponseProtocolDataUnit.x1());
        assertEquals(1008, sessionResponseProtocolDataUnit.y1());
        assertEquals(90, sessionResponseProtocolDataUnit.rotationAngle1());
    }

    @Test
    @Order(4)
    void sessionInvalidStateOverBoundsTest() throws Exception {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, GameStateFlatBufferSerializable.newSTATE(801, 601, 15));

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(5)
    void sessionInvalidStateUnderBoundsTest() throws Exception {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, GameStateFlatBufferSerializable.newSTATE(-1, -1, -15));

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(6)
    void sessionValidStateMoveDeltaTest() throws Exception {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, GameStateFlatBufferSerializable.newSTATE(1000, 1008, 180));

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(1000, sessionResponseProtocolDataUnit.x1());
        assertEquals(1008, sessionResponseProtocolDataUnit.y1());
        assertEquals(180, sessionResponseProtocolDataUnit.rotationAngle1());

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, GameStateFlatBufferSerializable.newSTATE(1008, 1008, 270));

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(1008, sessionResponseProtocolDataUnit.x1());
        assertEquals(1008, sessionResponseProtocolDataUnit.y1());
        assertEquals(270, sessionResponseProtocolDataUnit.rotationAngle1());
    }

    @Test
    @Order(7)
    void sessionInvalidStateMoveDeltaTest() throws Exception {

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, GameStateFlatBufferSerializable.newSTATE(2, 6, 45));

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, GameStateFlatBufferSerializable.newSTATE(4, 4, 45));

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(8)
    void sessionEndTest() throws Exception {

        final var sessionRequestProtocolDataUnit = GameStateFlatBufferSerializable.newSTOP();

        unicastToServerChannel1.invoke(sessionServerChannelHandler1, sessionRequestProtocolDataUnit);

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STOP, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(9)
    void joinSessionTest() throws Exception {

        final var instanceField = GameClientBootstrap.class.getDeclaredField("instance");

        instanceField.setAccessible(true);
        instanceField.set(player1, null);

        sessionResponseProtocolDataUnitQueue2 = new LinkedList<>();

        (player2 = GameClientBootstrap.newInstance())
                .withDecoder(new SessionServerChannelHandler.SessionResponseFlatBuffersDecoder())
                .withChannelHandler(sessionServerChannelHandler2 = new ServerChannelHandler<>() {

                    {

                        try {
                            (
                                    unicastToServerChannel2 =
                                            getClass()
                                                    .getSuperclass()
                                                    .getDeclaredMethod("unicastToServerChannel", FlatBufferSerializable.class)
                            )
                                    .setAccessible(true);
                        } catch (final NoSuchMethodException noSuchMethodException) {

                            logger.error(noSuchMethodException.getMessage(), noSuchMethodException);
                        }
                    }

                    @Override
                    protected void serverChannelRead(final GameStateFlatBufferSerializable.SessionResponseFlatBufferSerializable protocolDataUnit) {

                        sessionResponseProtocolDataUnitQueue2.offer(protocolDataUnit);
                    }
                })
                .withEncoder(new SessionServerChannelHandler.SessionRequestFlatBuffersEncoder())
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 4, GameStateFlatBufferSerializable.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 5, GameStateFlatBufferSerializable.SessionResponseFlatBufferSerializable.class)
                .run(1, 9999);

        unicastToServerChannel1.invoke(
                GameExampleNetworkedGameStateHandlerTest.sessionServerChannelHandler1,
                GameStateFlatBufferSerializable.newSTART("Test")
        );

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.START, sessionResponseProtocolDataUnit.sessionFlag());

        assertNotNull(sessionResponseProtocolDataUnit.sessionUUID());

        final var sessionUUID = sessionResponseProtocolDataUnit.sessionUUID();

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());

        unicastToServerChannel2.invoke(
                sessionServerChannelHandler2,
                GameStateFlatBufferSerializable.newJOIN("Test2", sessionUUID)
        );

        while (sessionResponseProtocolDataUnitQueue2.isEmpty())
            TimeUnit.MILLISECONDS.sleep(33);

        var sessionResponseProtocolDataUnit1 = sessionResponseProtocolDataUnitQueue2.poll();

        assertNotNull(sessionResponseProtocolDataUnit1);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.JOIN, sessionResponseProtocolDataUnit1.sessionFlag());

        assertNotNull(sessionResponseProtocolDataUnit1.nickname1());
        assertNotNull(sessionResponseProtocolDataUnit1.nickname2());

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit2 = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit2);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.JOIN, sessionResponseProtocolDataUnit2.sessionFlag());
        assertEquals("Test", sessionResponseProtocolDataUnit2.nickname1());
        assertEquals("Test2", sessionResponseProtocolDataUnit2.nickname2());

        blockUntilSessionResponseReceived();

        while(sessionResponseProtocolDataUnitQueue2.isEmpty())
            TimeUnit.MILLISECONDS.sleep(33);

        sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();
        sessionResponseProtocolDataUnit1 = sessionResponseProtocolDataUnitQueue2.poll();

        assertNotNull(sessionResponseProtocolDataUnit);
        assertNotNull(sessionResponseProtocolDataUnit1);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @RepeatedTest(100)
    @Order(10)
    void twoPlayerSessionTest() throws Exception {

        unicastToServerChannel1.invoke(
                sessionServerChannelHandler1,
                GameStateFlatBufferSerializable.newSTATE(x1 + 8, y1, 90)
        );

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit1 = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit1);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit1.sessionFlag());

        while (sessionResponseProtocolDataUnitQueue2.isEmpty()) {

            TimeUnit.MILLISECONDS.sleep(33);
        }

        var sessionResponseProtocolDataUnit2 = sessionResponseProtocolDataUnitQueue2.poll();

        assertNotNull(sessionResponseProtocolDataUnit2);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit2.sessionFlag());
        assertEquals(x1 + 8, sessionResponseProtocolDataUnit1.x1());
        assertEquals(y1, sessionResponseProtocolDataUnit1.y1());
        assertEquals(90, sessionResponseProtocolDataUnit1.rotationAngle1());
        assertEquals(x1 + 8, sessionResponseProtocolDataUnit2.x1());
        assertEquals(y1, sessionResponseProtocolDataUnit2.y1());
        assertEquals(90, sessionResponseProtocolDataUnit2.rotationAngle1());

        x1 = sessionResponseProtocolDataUnit1.x1();
        y1 = sessionResponseProtocolDataUnit1.y1();

        unicastToServerChannel2.invoke(
                sessionServerChannelHandler2,
                GameStateFlatBufferSerializable.newSTATE(x2, y2 + 8, 90)
        );

        while (sessionResponseProtocolDataUnitQueue2.isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(33);
        }

        sessionResponseProtocolDataUnit2 = sessionResponseProtocolDataUnitQueue2.poll();

        assertNotNull(sessionResponseProtocolDataUnit2);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit2.sessionFlag());

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit1 = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit1);

        assertEquals(GameStateFlatBufferSerializable.SessionFlag.STATE, sessionResponseProtocolDataUnit1.sessionFlag());
        assertEquals(x2, sessionResponseProtocolDataUnit1.x2());
        assertEquals(y2 + 8, sessionResponseProtocolDataUnit1.y2());
        assertEquals(90, sessionResponseProtocolDataUnit1.rotationAngle1());
        assertEquals(x2, sessionResponseProtocolDataUnit2.x2());
        assertEquals(y2 + 8, sessionResponseProtocolDataUnit2.y2());
        assertEquals(90, sessionResponseProtocolDataUnit2.rotationAngle1());

        x2 = sessionResponseProtocolDataUnit2.x2();
        y2 = sessionResponseProtocolDataUnit2.y2();
    }

    @Test
    @Order(11)
    void shutdownJoinedPlayerGameClientTest() {

        player2.shutdownGracefully();
    }*/

    void blockUntilSessionResponseReceived() throws Exception {

        while (sessionResponseProtocolDataUnitQueue1.isEmpty())
            TimeUnit.MILLISECONDS.sleep(33);
    }
}
