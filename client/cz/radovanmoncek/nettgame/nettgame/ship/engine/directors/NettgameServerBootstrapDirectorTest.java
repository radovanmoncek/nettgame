package cz.radovanmoncek.nettgame.nettgame.ship.engine.directors;

import cz.radovanmoncek.nettgame.nettgame.ship.engine.bootstrap.NettgameServerBootstrap;
import cz.radovanmoncek.nettgame.nettgame.ship.engine.builders.NettgameServerBootstrapBuilder;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.utilities.reflection.ReflectionUtilities;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class NettgameServerBootstrapDirectorTest {
    private static NettgameServerBootstrapDirector director;

    @BeforeEach
    void setUp() {

        director = new NettgameServerBootstrapDirector(new NettgameServerBootstrapBuilder());
    }

    @Test
    void verifyBuilder() throws NoSuchFieldException, IllegalAccessException {

        assertNotNull(director);

        final var builderField = director
                .getClass()
                .getDeclaredField("builder");

        builderField.setAccessible(true);

        assertNotNull(builderField.get(director));

        assertEquals(NettgameServerBootstrapBuilder.class, builderField.get(director).getClass());
    }

    @Test
    void makeDefaultGameServerBootstrap() throws NoSuchFieldException, IllegalAccessException {

        NettgameServerBootstrap bootstrap;

        try {

            bootstrap = director
                    .makeDefaultGameServerBootstrap()
                    .build();

            assertEquals(InetAddress.getLocalHost().getHostAddress(), ((InetAddress) ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "address")).getHostAddress());
        } catch (UnknownHostException e) {

            bootstrap = director
                    .makeLoopbackGameServerBootstrap()
                    .build();
            assertEquals(InetAddress.getLoopbackAddress().getHostAddress(), ((InetAddress) ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "address")).getHostAddress());
        }

        assertNotNull(bootstrap);

        assertEquals(4321, ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "port"));
        assertEquals(4, ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "shutdownTimeout"));
        assertTrue(((LinkedList<?>) ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "initialHandlers")).stream().map(Object::getClass).anyMatch(LoggingHandler.class::equals));
        assertTrue(((LinkedList<?>) ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "repositories")).isEmpty());
    }

    @Test
    void makeLoopbackGameServerBootstrap() throws NoSuchFieldException, IllegalAccessException {

        ReflectionUtilities.setValueOnFieldReflectively(NettgameServerBootstrap.class, "instance", null);

        final var bootstrap = director
                .makeLoopbackGameServerBootstrap()
                .build();

        assertNotNull(bootstrap);

        assertEquals(4321, ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "port"));
        assertEquals(4, ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "shutdownTimeout"));
        assertEquals(InetAddress.getLoopbackAddress().getHostAddress(), ((InetAddress) ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "address")).getHostAddress());
        assertTrue(((LinkedList<?>) ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "initialHandlers")).stream().map(Object::getClass).anyMatch(LoggingHandler.class::equals));
        assertTrue(((LinkedList<?>) ReflectionUtilities.returnValueOnFieldReflectively(bootstrap, "repositories")).isEmpty());
    }
}
