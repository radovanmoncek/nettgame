package cz.radovanmoncek.client.ship.bootstrap;

import com.sun.istack.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

public class GameClientBootstrapTest {
    private static GameClientBootstrap gameClientBootstrap;

    @BeforeAll
    static void setup() {

        gameClientBootstrap = GameClientBootstrap.returnNewInstance();
    }

    @Test
    void singletonTest() {

        assertEquals(GameClientBootstrap.returnNewInstance(), gameClientBootstrap);
    }

    @Test
    void withPortTest() throws NoSuchFieldException, IllegalAccessException {

        gameClientBootstrap.setGameServerPort(4321);

        assertEquals(4321, returnEncapsulatedField(gameClientBootstrap.getClass(), "gameServerPort").get(gameClientBootstrap));

        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setGameServerPort(-1));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setGameServerPort(21));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setGameServerPort(53));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setGameServerPort(443));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setGameServerPort(1024));
        assertThrows(IllegalArgumentException.class, () -> gameClientBootstrap.setGameServerPort(65536));

        assertDoesNotThrow(() -> gameClientBootstrap.setGameServerPort(54321));
    }

    @Test
    void withServerAddressTest() throws NoSuchFieldException, IllegalAccessException {

        final var address = InetAddress.getLoopbackAddress();

        gameClientBootstrap.setGameServerAddress(address);

        assertEquals(address, returnEncapsulatedField(gameClientBootstrap.getClass(), "gameServerAddress").get(gameClientBootstrap));
    }

    private static Field returnEncapsulatedField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {

        final var field = clazz.getDeclaredField(fieldName);

        field.setAccessible(true);

        return field;
    }
}
