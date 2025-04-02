package cz.radovanmoncek.ship.engine.builders;

import cz.radovanmoncek.ship.bay.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.bay.repositories.Repository;
import cz.radovanmoncek.ship.bay.utilities.logging.LoggingUtilities;
import cz.radovanmoncek.ship.bay.utilities.reflection.ReflectionUtilities;
import io.netty.channel.ChannelHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class NettgameServerBootstrapBuilderTest {
    private static NettgameServerBootstrapBuilder nettgameServerBootstrapBuilder;

    @BeforeEach
    void setUp() {

        nettgameServerBootstrapBuilder = new NettgameServerBootstrapBuilder();

        LoggingUtilities.enableGlobalLoggingLevel(Level.ALL);
    }

    @AfterEach
    void resetInstance() {

        nettgameServerBootstrapBuilder.reset();
    }

    @Test
    void build() {

        final var result = nettgameServerBootstrapBuilder.build();

        assertNotNull(result);
    }

    @Test
    void reset() {

        final var lastInstance = nettgameServerBootstrapBuilder.build();

        nettgameServerBootstrapBuilder.reset();

        assertNotEquals(lastInstance, nettgameServerBootstrapBuilder.build());
    }

    @Test
    void buildPort() throws NoSuchFieldException, IllegalAccessException {

        nettgameServerBootstrapBuilder.buildPort(4321);

        assertEquals(4321, ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrapBuilder.build(), "port"));
    }

    @Test
    void buildInternetProtocolAddress() throws NoSuchFieldException, IllegalAccessException {

        nettgameServerBootstrapBuilder.buildInternetProtocolAddress(InetAddress.getLoopbackAddress());

        assertEquals(InetAddress.getLoopbackAddress(), ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrapBuilder.build(), "address"));
    }

    @Test
    void buildChannelHandlerCreator() throws NoSuchFieldException, IllegalAccessException {

        final var channelHandlerCreator = new ChannelHandlerCreator(){

            @Override
            public ChannelHandler newProduct() {
                return null;
            }
        };

        nettgameServerBootstrapBuilder.buildChannelHandlerCreator(channelHandlerCreator);

        assertEquals(channelHandlerCreator, ((LinkedList<?>) ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrapBuilder.build(), "channelHandlerCreators")).getFirst());
    }

    @Test
    void buildRepository() throws NoSuchFieldException, IllegalAccessException {

        final var repository = new Repository<>() {

                    /*@Override
                    public Class<Object> returnEntityClass() {
                        return null;
                    }*/
                };

        nettgameServerBootstrapBuilder.buildRepository(repository);

        assertEquals(repository, ((LinkedList<?>) ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrapBuilder.build(), "repositories")).getFirst());
    }

    @Test
    void buildLogLevel() {
    }
}
