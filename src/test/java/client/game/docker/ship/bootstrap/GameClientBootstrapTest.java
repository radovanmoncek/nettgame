package client.game.docker.ship.bootstrap;

import client.game.docker.ship.parents.handlers.ServerChannelHandler;
import container.game.docker.ship.parents.models.FlatBufferSerializable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class GameClientBootstrapTest {
    private static GameClientBootstrap gameClientBootstrap;

    @BeforeAll
    static void setup() {

        gameClientBootstrap = GameClientBootstrap.newInstance();
    }

    @Test
    void singletonTest() {

        assertEquals(GameClientBootstrap.newInstance(), gameClientBootstrap);
    }

    @Test
    void withPortTest() {

        gameClientBootstrap.withServerPort(4321);

        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.withServerPort(-1));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.withServerPort(21));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.withServerPort(65536));

        assertDoesNotThrow(() -> gameClientBootstrap.withServerPort(54321));
    }

    @Test
    void withServerAddressTest(){

        gameClientBootstrap.withInstanceContainerAddress(InetAddress.getLoopbackAddress());
    }

    @Test
    void withChannelHandlerTest() throws Exception {

        gameClientBootstrap.withChannelHandler(new ServerChannelHandler<>() {

            @Override
            protected void serverChannelRead(Object message) {

            }
        });

        final var channelHandlersField = gameClientBootstrap.getClass().getDeclaredField("channelHandlers");

        channelHandlersField.setAccessible(true);

        assertNotNull(channelHandlersField.get(gameClientBootstrap));

        assertEquals(1, ((LinkedList<?>) channelHandlersField.get(gameClientBootstrap)).size());
        assertEquals(ServerChannelHandler.class, ((LinkedList<?>) channelHandlersField.get(gameClientBootstrap)).getFirst().getClass().getSuperclass());
    }
}
