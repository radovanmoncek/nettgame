package client.game.docker.ship.bootstrap;

import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class GameClientTest {
    private static GameClient gameClient;

    @BeforeAll
    static void setup() {

        gameClient = GameClient.newInstance();
    }

    @Test
    void singletonTest() {

        assertEquals(GameClient.newInstance(), gameClient);
    }

    @Test
    void withPortTest() {

        gameClient.withServerPort(4321);

        assertThrows(IllegalArgumentException.class, () -> gameClient.withServerPort(-1));
        assertThrows(IllegalArgumentException.class, () -> gameClient.withServerPort(21));
        assertThrows(IllegalArgumentException.class, () -> gameClient.withServerPort(65536));

        assertDoesNotThrow(() -> gameClient.withServerPort(54321));
    }

    @Test
    void withServerAddressTest(){

        gameClient.withInstanceContainerAddress(InetAddress.getLoopbackAddress());
    }

    @Test
    void withChannelHandlerTest() throws Exception {

        gameClient.withChannelHandler(new ChannelHandler<>() {

            @Override
            protected void serverChannelRead(ProtocolDataUnit protocolDataUnit) {

            }
        });

        final var channelHandlersField = gameClient.getClass().getDeclaredField("channelHandlers");

        channelHandlersField.setAccessible(true);

        assertNotNull(channelHandlersField.get(gameClient));

        assertEquals(1, ((LinkedList<?>) channelHandlersField.get(gameClient)).size());
        assertEquals(ChannelHandler.class, ((LinkedList<?>) channelHandlersField.get(gameClient)).getFirst().getClass().getSuperclass());
    }
}
