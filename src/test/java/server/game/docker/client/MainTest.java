package server.game.docker.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.game.docker.client.modules.requests.facades.LobbyReqFacade;

import java.util.Timer;
import java.util.TimerTask;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    private static GameClient gameClient;

    @BeforeAll
    static void setup() throws Exception {
        gameClient = GameClient.getInstance()
                .withLobbyReqFacade(new LobbyReqFacade());
        //new Thread(() -> {
        while (gameClient.getClientChannel() == null) {
            try {
                gameClient.setClientChannel(gameClient.getBootstrap().connect(gameClient.getServerAddress(), gameClient.getGameServerPort()).sync().channel());
                Thread.sleep(1000);
                //        gameClient.setClientChannel(gameClient.getClientChannel());
//                gameClient.getWorkerGroup().shutdownGracefully();
            } catch (Exception ignored) {
            }
        }
        while(gameClient.getAssignedID() == null){
            Thread.sleep(1000);
        }
        //        gameClient.getClientChannel().close();
//        try {
//            gameClient.getClientChannel().closeFuture().sync();
//        } catch (InterruptedException ignored) {
//        }
        //}).start();
    }

    @Test
    void connectionTest() throws Exception {
        assertTrue(gameClient.getClientChannel() != null && gameClient.getClientChannel().isActive());
    }

    @Test
    void iDReceivedTest() throws Exception {
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
