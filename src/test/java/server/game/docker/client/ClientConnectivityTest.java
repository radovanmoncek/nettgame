package server.game.docker.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.client.modules.player.facades.PlayerClientFacade;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ClientConnectivityTest {
    private static GameClient gameClient;
    private static String nickname;
    private static Boolean inLobby;

    @BeforeAll
    static void setup() throws Exception {
        gameClient = GameClient
                .newInstance()
                .withUsernameFacade(new PlayerClientFacade(){
                    @Override
                    public void receiveNewNickname(String newNickname) {
                        nickname = newNickname;
                    }
                })
                .withLobbyReqFacade(new LobbyClientFacade(){
                    @Override
                    public void receiveLobbyCreated(Long leaderId, Collection<String> members) {
                        inLobby = true;
                    }
                });

        gameClient.run(1);
    }

    @Test
    void connectionTest() {
        assertTrue(gameClient.isConnected());
    }

    @Test
    void usernameRequestAndReceiveTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> gameClient.getUsernameClientFacade().requestNickname("TestThatIsWayOverTheLimitOf8Characters"));
        gameClient.getUsernameClientFacade().requestNickname("Test");

        for (int i = 0; i < 10 && nickname == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }
        assertEquals("Test", nickname);
    }

    @Test
    void createLobbyTest() throws Exception {
        gameClient.getLobbyReqFacade().createLobby();
        for (int i = 0; i < 10 && inLobby == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }
        assertNotNull(inLobby);
    }

    @AfterAll
    static void tearDown() throws Exception {
        gameClient.shutdownGracefullyAfterNSeconds(10);
    }
}
