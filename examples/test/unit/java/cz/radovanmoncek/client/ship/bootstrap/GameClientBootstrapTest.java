package cz.radovanmoncek.client.ship.bootstrap;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.client.ship.bootstrap.GameClientBootstrap;
import cz.radovanmoncek.client.ship.parents.handlers.ServerChannelHandler;
import io.netty.channel.ChannelHandlerContext;
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

        gameClientBootstrap.setServerPort(4321);

        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setServerPort(-1));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setServerPort(21));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setServerPort(65536));

        assertDoesNotThrow(() -> gameClientBootstrap.setServerPort(54321));
    }

    @Test
    void withServerAddressTest(){

        gameClientBootstrap.setInstanceContainerAddress(InetAddress.getLoopbackAddress());
    }

    @Test
    void addChannelHandlerTest() throws Exception {

        gameClientBootstrap.addChannelHandler(new ServerChannelHandler<>() {

            @Override
            protected void channelRead0(final ChannelHandlerContext channelHandlerContext, Table table) {

            }
        });

        final var channelHandlersField = gameClientBootstrap.getClass().getDeclaredField("channelHandlers");

        channelHandlersField.setAccessible(true);

        assertNotNull(channelHandlersField.get(gameClientBootstrap));

        assertEquals(1, ((LinkedList<?>) channelHandlersField.get(gameClientBootstrap)).size());
        assertEquals(ServerChannelHandler.class, ((LinkedList<?>) channelHandlersField.get(gameClientBootstrap)).getFirst().getClass().getSuperclass());
    }
}
