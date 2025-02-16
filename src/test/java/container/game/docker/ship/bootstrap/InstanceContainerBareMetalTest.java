package container.game.docker.ship.bootstrap;

import container.game.docker.ship.data.structures.MultiValueTypeMap;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstanceContainerBareMetalTest {
    private static InstanceContainer instanceContainer;

    @BeforeAll
    static void setup(){

        instanceContainer = InstanceContainer.newInstance();
    }

    @Test
    void singletonTest() throws Exception {

        final var instanceField = instanceContainer.getClass().getDeclaredField("instance");

        instanceField.setAccessible(true);

        final var instance = (InstanceContainer) instanceField.get(instanceContainer);

        assertNotNull(instance);

        assertEquals(instance, instanceContainer);
        assertEquals(instance, InstanceContainer.newInstance());
    }

    @Test
    void handlerFactoryAdditionTest() throws Exception {

        instanceContainer.withChannelGroupHandlerFactory(() -> new ChannelGroupHandler<>() {

            @Override
            protected void playerChannelRead(ProtocolDataUnit protocolDataUnit, MultiValueTypeMap playerSession) {

            }

            @Override
            protected void playerDisconnected(MultiValueTypeMap playerSession) {

            }
        });

        final var instanceField = instanceContainer.getClass().getDeclaredField("channelGroupHandlerSuppliers");

        instanceField.setAccessible(true);

        final var channelGroupHandlerSuppliers = (LinkedList<?>) instanceField.get(instanceContainer);

        assertEquals(1, channelGroupHandlerSuppliers.size());
    }
}
