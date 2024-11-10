package server.game.docker.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ClientConnectivityTest {
    private static GameClient gameClient;

    @BeforeAll
    static void setup() throws Exception {
        (
                gameClient = GameClient
                .newInstance()
        ).run(1);
    }

    @Test
    void connectionTest() {
        assertTrue(gameClient.isConnected());
    }

    @Test
    void usernameRequestAndReceiveTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> gameClient.getUsernameClientFacade().requestUsername("TestThatIsWayOverTheLimitOf8Characters"));
        gameClient.getUsernameClientFacade().requestUsername("Test");

        for (int i = 0; i < 10 && gameClient.getUsernameClientFacade().getClientUsername() == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }
        assertEquals("Test", gameClient.getUsernameClientFacade().getClientUsername());
    }

    @Test
    void createLobbyTest() throws Exception {
        gameClient.getLobbyReqFacade().createLobby();
        for (int i = 0; i < 10 && gameClient.getLobbyReqFacade().getLobbyID() == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }
        assertNotNull(gameClient.getLobbyReqFacade().getLobbyID());
    }

    @AfterAll
    static void tearDown() throws Exception {
        gameClient.shutdownGracefullyAfterNSeconds(10);
    }
}
