package container.game.docker.modules.examples.sessions.handlers;

import client.game.docker.modules.examples.sessions.codecs.SessionRequestEncoder;
import client.game.docker.modules.examples.sessions.codecs.SessionResponseDecoder;
import client.game.docker.ship.bootstrap.GameClient;
import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.sessions.models.SessionFlag;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.bootstrap.examples.SampleInstanceContainer;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionChannelGroupHandlerTest {
    private static GameClient player1, player2;
    private static Method unicastToServerChannel1, unicastToServerChannel2;
    private static ChannelHandler<SessionResponseProtocolDataUnit, SessionRequestProtocolDataUnit> sessionChannelHandler1, sessionChannelHandler2;
    private static Queue<SessionResponseProtocolDataUnit> sessionResponseProtocolDataUnitQueue1, sessionResponseProtocolDataUnitQueue2;
    private static int x1 = 0, y1 = 0, x2 = 0, y2 = 0;

    @BeforeAll
    static void setup() throws Exception {

        SampleInstanceContainer.main(null);

        sessionResponseProtocolDataUnitQueue1 = new LinkedList<>();

        (player1 = GameClient.newInstance())
                .withDecoder(SessionResponseDecoder::new)
                .withChannelHandler(() -> sessionChannelHandler1 = new ChannelHandler<>() {

                    {

                        try {

                            unicastToServerChannel1 = getClass()
                                    .getSuperclass()
                                    .getDeclaredMethod("unicastToServerChannel", ProtocolDataUnit.class);
                        } catch (NoSuchMethodException noSuchMethodException) {

                            noSuchMethodException.printStackTrace(); //todo: log4j
                        }

                        unicastToServerChannel1.setAccessible(true);
                    }

                    @Override
                    protected void serverChannelRead(final SessionResponseProtocolDataUnit protocolDataUnit) {

                        sessionResponseProtocolDataUnitQueue1.offer(protocolDataUnit);
                    }
                })
                .withEncoder(SessionRequestEncoder::new)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 4, SessionRequestProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 5, SessionResponseProtocolDataUnit.class)
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
                        sessionChannelHandler1,
                        SessionRequestProtocolDataUnit.newSTART("VeryLongNicknameThatIsOverEightCharacters")
                );

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(2)
    void sessionStartTest() throws Exception {

        final var sessionRequestProtocolDataUnit = SessionRequestProtocolDataUnit.newSTART("Test");

        unicastToServerChannel1.invoke(sessionChannelHandler1, sessionRequestProtocolDataUnit);

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.START, sessionResponseProtocolDataUnit.sessionFlag());

        assertNotNull(sessionResponseProtocolDataUnit.sessionUUID());

        assertEquals("Test", sessionResponseProtocolDataUnit.nickname1());
    }

    @AfterAll
    static void tearDown() {

        player1.shutdownGracefullyAfterNSeconds(0);
    }

    @Test
    @Order(3)
    void sessionValidStateWithinBoundsTest() throws Exception {

        unicastToServerChannel1.invoke(sessionChannelHandler1, SessionRequestProtocolDataUnit.newSTATE(4, 4, 90));

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(4, sessionResponseProtocolDataUnit.x1());
        assertEquals(4, sessionResponseProtocolDataUnit.y1());
        assertEquals(90, sessionResponseProtocolDataUnit.rotationAngle1());
    }

    @Test
    @Order(4)
    void sessionInvalidStateOverBoundsTest() throws Exception {

        unicastToServerChannel1.invoke(sessionChannelHandler1, SessionRequestProtocolDataUnit.newSTATE(801, 601, 15));

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(5)
    void sessionInvalidStateUnderBoundsTest() throws Exception {

        unicastToServerChannel1.invoke(sessionChannelHandler1, SessionRequestProtocolDataUnit.newSTATE(-1, -1, -15));

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(6)
    void sessionValidStateMoveDeltaTest() throws Exception {

        unicastToServerChannel1.invoke(sessionChannelHandler1, SessionRequestProtocolDataUnit.newSTATE(0, 4, 180));

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(0, sessionResponseProtocolDataUnit.x1());
        assertEquals(4, sessionResponseProtocolDataUnit.y1());
        assertEquals(180, sessionResponseProtocolDataUnit.rotationAngle1());

        unicastToServerChannel1.invoke(sessionChannelHandler1, SessionRequestProtocolDataUnit.newSTATE(4, 4, 270));

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.STATE, sessionResponseProtocolDataUnit.sessionFlag());
        assertEquals(4, sessionResponseProtocolDataUnit.x1());
        assertEquals(4, sessionResponseProtocolDataUnit.y1());
        assertEquals(270, sessionResponseProtocolDataUnit.rotationAngle1());
    }

    @Test
    @Order(7)
    void sessionInvalidStateMoveDeltaTest() throws Exception {

        unicastToServerChannel1.invoke(sessionChannelHandler1, SessionRequestProtocolDataUnit.newSTATE(2, 6, 45));

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());

        unicastToServerChannel1.invoke(sessionChannelHandler1, SessionRequestProtocolDataUnit.newSTATE(4, 4, 45));

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.INVALID, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(8)
    void sessionEndTest() throws Exception {

        final var sessionRequestProtocolDataUnit = SessionRequestProtocolDataUnit.newSTOP();

        unicastToServerChannel1.invoke(sessionChannelHandler1, sessionRequestProtocolDataUnit);

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.STOP, sessionResponseProtocolDataUnit.sessionFlag());
    }

    @Test
    @Order(9)
    void  joinSessionTest() throws Exception {

        final var instanceField = GameClient.class.getDeclaredField("instance");

        instanceField.setAccessible(true);
        instanceField.set(player1, null);

        sessionResponseProtocolDataUnitQueue2 = new LinkedList<>();

        (player2 = GameClient.newInstance())
                .withDecoder(SessionResponseDecoder::new)
                .withChannelHandler(() -> sessionChannelHandler2 = new ChannelHandler<>() {

                    {

                        try {
                            (
                                    unicastToServerChannel2 =
                                            getClass()
                                                    .getSuperclass()
                                                    .getDeclaredMethod("unicastToServerChannel", ProtocolDataUnit.class)
                            )
                                    .setAccessible(true);
                        } catch (NoSuchMethodException noSuchMethodException) {

                            noSuchMethodException.printStackTrace(); //todo: log4j
                        }
                    }

                    @Override
                    protected void serverChannelRead(final SessionResponseProtocolDataUnit protocolDataUnit) {

                        sessionResponseProtocolDataUnitQueue2.offer(protocolDataUnit);
                    }
                })
                .withEncoder(SessionRequestEncoder::new)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 4, SessionRequestProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 5, SessionResponseProtocolDataUnit.class)
                .run(1, 9999);

        unicastToServerChannel1.invoke(
                SessionChannelGroupHandlerTest.sessionChannelHandler1,
                SessionRequestProtocolDataUnit.newSTART("Test")
        );

        blockUntilSessionResponseReceived();

        final var sessionResponseProtocolDataUnit = SessionChannelGroupHandlerTest.sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit);

        assertEquals(SessionFlag.START, sessionResponseProtocolDataUnit.sessionFlag());

        assertNotNull(sessionResponseProtocolDataUnit.sessionUUID());

        unicastToServerChannel2.invoke(
                sessionChannelHandler2,
                SessionRequestProtocolDataUnit.newJOIN("Test2", sessionResponseProtocolDataUnit.sessionUUID())
        );

        while (sessionResponseProtocolDataUnitQueue2.isEmpty())
            TimeUnit.MILLISECONDS.sleep(33);

        var sessionResponseProtocolDataUnit1 = sessionResponseProtocolDataUnitQueue2.poll();

        assertNotNull(sessionResponseProtocolDataUnit1);

        assertEquals(SessionFlag.JOIN, sessionResponseProtocolDataUnit1.sessionFlag());

        assertNotNull(sessionResponseProtocolDataUnit1.nickname1());
        assertNotNull(sessionResponseProtocolDataUnit1.nickname2());

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit2 = SessionChannelGroupHandlerTest.sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit2);

        assertEquals(SessionFlag.JOIN, sessionResponseProtocolDataUnit2.sessionFlag());
        assertEquals("Test", sessionResponseProtocolDataUnit2.nickname1());
        assertEquals("Test2", sessionResponseProtocolDataUnit2.nickname2());
    }

    @RepeatedTest(100)
    @Order(10)
    void twoPlayerSessionTest() throws Exception {

        unicastToServerChannel1.invoke(
                sessionChannelHandler1,
                SessionRequestProtocolDataUnit.newSTATE(x1 + 4, y1, 90)
        );

        blockUntilSessionResponseReceived();

        var sessionResponseProtocolDataUnit1 = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit1);

        assertEquals(SessionFlag.STATE, sessionResponseProtocolDataUnit1.sessionFlag());

        while (sessionResponseProtocolDataUnitQueue2.isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(33);
        }

        var sessionResponseProtocolDataUnit2 = sessionResponseProtocolDataUnitQueue2.poll();

        assertNotNull(sessionResponseProtocolDataUnit2);

        assertEquals(SessionFlag.STATE, sessionResponseProtocolDataUnit2.sessionFlag());
        assertEquals(x1 + 4, sessionResponseProtocolDataUnit1.x1());
        assertEquals(y1, sessionResponseProtocolDataUnit1.y1());
        assertEquals(90, sessionResponseProtocolDataUnit1.rotationAngle1());
        assertEquals(x1 + 4, sessionResponseProtocolDataUnit2.x1());
        assertEquals(y1, sessionResponseProtocolDataUnit2.y1());
        assertEquals(90, sessionResponseProtocolDataUnit2.rotationAngle1());

        x1 = sessionResponseProtocolDataUnit1.x1();
        y1 = sessionResponseProtocolDataUnit1.y1();

        unicastToServerChannel2.invoke(
                sessionChannelHandler2,
                SessionRequestProtocolDataUnit.newSTATE(x2, y2 + 4, 90)
        );

        while (sessionResponseProtocolDataUnitQueue2.isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(33);
        }

        sessionResponseProtocolDataUnit2 = sessionResponseProtocolDataUnitQueue2.poll();

        assertNotNull(sessionResponseProtocolDataUnit2);

        assertEquals(SessionFlag.STATE, sessionResponseProtocolDataUnit2.sessionFlag());

        blockUntilSessionResponseReceived();

        sessionResponseProtocolDataUnit1 = sessionResponseProtocolDataUnitQueue1.poll();

        assertNotNull(sessionResponseProtocolDataUnit1);

        assertEquals(SessionFlag.STATE, sessionResponseProtocolDataUnit1.sessionFlag());
        assertEquals(x2, sessionResponseProtocolDataUnit1.x2());
        assertEquals(y2 + 4, sessionResponseProtocolDataUnit1.y2());
        assertEquals(90, sessionResponseProtocolDataUnit1.rotationAngle1());
        assertEquals(x2, sessionResponseProtocolDataUnit2.x2());
        assertEquals(y2 + 4, sessionResponseProtocolDataUnit2.y2());
        assertEquals(90, sessionResponseProtocolDataUnit2.rotationAngle1());

        x2 = sessionResponseProtocolDataUnit2.x2();
        y2 = sessionResponseProtocolDataUnit2.y2();
    }

    @Test
    @Order(11)
    void shutdownJoinedPlayerGameClientTest() {

        player2.shutdownGracefully();
    }

    void blockUntilSessionResponseReceived() throws Exception {

        while (sessionResponseProtocolDataUnitQueue1.isEmpty())
            TimeUnit.MILLISECONDS.sleep(33);
    }
}
