package client.game.docker.ship.bootstrap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameClientTest {
    private static GameClient gameClient;

    @BeforeAll
    static void setup() throws Exception {

        gameClient = GameClient.newInstance();
    }

    @Test
    void singletonTest() throws Exception {

        assertEquals(GameClient.newInstance(), gameClient);
    }

    @Test
    void withPortTest() {

        gameClient.withServerPort(4321);

        assertThrows(Exception.class, () -> gameClient.withServerPort(-1));
        assertThrows(Exception.class, () -> gameClient.withServerPort(65535));
    }

    @Test
    void withServerAddressTest(){

        gameClient.withInstanceContainerAddress(InetAddress.getLoopbackAddress());
    }
}
