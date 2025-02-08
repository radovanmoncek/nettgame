package client.game.docker.modules.sessions.handlers;

import client.game.docker.modules.examples.sessions.handlers.SessionChannelHandler;
import client.game.docker.ship.bootstrap.GameClient;
import client.game.docker.ship.bootstrap.examples.SampleGameClient;
import container.game.docker.modules.examples.session.models.SessionFlag;
import container.game.docker.modules.examples.session.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.session.models.SessionResponseProtocolDataUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SessionChannelHandlerTest {
    private static GameClient gameClient;
    private static Method unicastToServerChannel;
    private static SessionResponseProtocolDataUnit sessionResponseProtocolDataUnit;

    @BeforeAll
    static void setup() throws Exception {

        final var sampleGameClient = new SampleGameClient();

        (gameClient = GameClient.newInstance())
                .withChannelHandler(() -> new SessionChannelHandler(sampleGameClient){

                    {

                        try {

                            SessionChannelHandlerTest.unicastToServerChannel = getClass().getSuperclass().getMethod("unicastToServerChannel");
                        } catch (NoSuchMethodException e) {

                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    protected void serverChannelRead(final SessionResponseProtocolDataUnit sessionResponseProtocolDataUnit) {

                        SessionChannelHandlerTest.sessionResponseProtocolDataUnit = sessionResponseProtocolDataUnit;
                    }
                })
                .run();
    }

    @Test
    void sessionStartTest() throws InterruptedException {

//        unicastToServerChannel.invoke(sessionChannelHandler, );

        TimeUnit.MILLISECONDS.sleep(500);

        assertEquals(SessionFlag.START, sessionResponseProtocolDataUnit.sessionFlag());

        assertNotNull(sessionResponseProtocolDataUnit.sessionHash());

        assertEquals("Test", sessionResponseProtocolDataUnit.nickname1());
    }

    @Test
    void tooLongNicknameRequest() throws Exception {

        unicastToServerChannel.invoke(sessionResponseProtocolDataUnit, new SessionRequestProtocolDataUnit(SessionFlag.START, null, null, null, null, "VeryLongNicknameThatIsOverEightCharacters"));
    }

    @AfterAll
    static void tearDown() throws Exception {

        gameClient.shutdownGracefullyAfterNSeconds(0);
    }
}
