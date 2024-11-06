package server.game.docker.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.game.docker.client.modules.requests.facades.LobbyReqClientFacade;

import java.util.Timer;
import java.util.TimerTask;

import static org.junit.jupiter.api.Assertions.*;

public class ClientConnectivityTest {
    private static GameClient gameClient;

    @BeforeAll
    static void setup() throws Exception {
        gameClient = GameClient.getInstance()
                .withLobbyReqFacade(new LobbyReqClientFacade());
        while (gameClient.getClientChannel() == null) {
            try {
                final var clientChannel = gameClient.getBootstrap().connect(gameClient.getServerAddress(), gameClient.getGameServerPort()).sync().channel();
                gameClient.setClientChannel(clientChannel);
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        while(gameClient.getAssignedID() == null){
            Thread.sleep(1000);
        }
    }

    @Test
    void connectionTest() {
        assertTrue(gameClient.getClientChannel() != null && gameClient.getClientChannel().isActive());
    }

    @Test
    void iDReceivedTest() {
        assertNotNull(gameClient.getAssignedID());
    }

    @Test
    void createLobbyTest() throws Exception {
        gameClient.getLobbyReqFacade().createLobby();
        Long createdLobbyID = null;
        for (int i = 0; i < 10 && createdLobbyID == null; i++) {
            Thread.sleep(1000);
        }
        assertNotNull(createdLobbyID);
    }

    @AfterAll
    static void tearDown() throws Exception {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                gameClient.getClientChannel().close();
            }
        }, 10000);
        gameClient.getClientChannel().closeFuture().sync();
        gameClient.getWorkerGroup().shutdownGracefully();
    }
}
