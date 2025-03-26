package cz.radovanmoncek.test.ship.creators;

import cz.radovanmoncek.server.ship.creators.ExampleGameSessionHandlerCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleGameSessionHandlerCreatorTest {

    @Test
    void newProduct() {

        final var handlerCreator = new ExampleGameSessionHandlerCreator();
        final var handler = handlerCreator.newProduct();

        assertNotNull(handler);

        assertInstanceOf(handler.getClass(), handler);
    }
}
