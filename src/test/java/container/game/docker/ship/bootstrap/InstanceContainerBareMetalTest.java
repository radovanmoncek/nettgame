package container.game.docker.ship.bootstrap;

import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.PlayerSessionData;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import container.game.docker.ship.parents.products.Product;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstanceContainerBareMetalTest {
    private static InstanceContainer instanceContainer;

    @BeforeAll
    static void setup() {

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

        Creator creator;

        instanceContainer.withChannelGroupHandlerCreator(creator = new Creator() {

            @Override
            public Product newProduct() {

                return new ChannelGroupHandler<>() {

                    @Override
                    protected void playerChannelRead(ProtocolDataUnit protocolDataUnit, PlayerSessionData playerSession) {

                    }

                    @Override
                    protected void playerDisconnected(PlayerSessionData playerSession) {

                    }
                };
            }
        });

        final var instanceField = instanceContainer.getClass().getDeclaredField("channelGroupHandlerSuppliers");

        instanceField.setAccessible(true);

        final var channelGroupHandlerSuppliers = (LinkedList<?>) instanceField.get(instanceContainer);

        assertEquals(1, channelGroupHandlerSuppliers.size());
        assertEquals(creator, channelGroupHandlerSuppliers.getFirst());
        assertEquals(ChannelGroupHandler.class, ((Creator) channelGroupHandlerSuppliers.getFirst()).newProduct().getClass().getSuperclass());

        instanceContainer.withDecoderCreator(new Creator() {

            @Override
            public Product newProduct() {

                return new Decoder<>(ProtocolDataUnit.class) {

                    @Override
                    protected void decodeBodyAfterHeader(ByteBuf in, List<? super ProtocolDataUnit> out) {}
                };
            }
        });

        assertEquals(2, channelGroupHandlerSuppliers.size());

        final var decoder = (Decoder<?>) ((Creator) channelGroupHandlerSuppliers.getLast()).newProduct();
        final var bindingsField = decoder.getClass().getSuperclass().getDeclaredField("protocolIdentifierToProtocolDataUnitBindings");

        bindingsField.setAccessible(true);

        assertNotNull(bindingsField.get(decoder));

        assertEquals(HashMap.class, bindingsField.get(decoder).getClass());

        instanceContainer.registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 2, ProtocolDataUnit.class);
        instanceContainer.withEncoderCreator(new Creator() {

            @Override
            public Product newProduct() {

                return new Encoder<>() {

                    @Override
                    protected void encodeBodyAfterHeader(ProtocolDataUnit protocolDataUnit, ByteBuf out) {}
                };
            }
        });

        assertEquals(3, channelGroupHandlerSuppliers.size());

        final var encoder = (Encoder<?>) ((Creator) channelGroupHandlerSuppliers.getLast()).newProduct();
        final var bindingsFieldEncoder = encoder.getClass().getSuperclass().getDeclaredField("protocolDataUnitToProtocolIdentifierBindings");

        bindingsFieldEncoder.setAccessible(true);

        assertNotNull(bindingsFieldEncoder.get(encoder));

        assertEquals(HashMap.class, bindingsFieldEncoder.get(encoder).getClass());
        assertEquals((byte) 2, ((HashMap<?, ?>) bindingsFieldEncoder.get(encoder)).get(ProtocolDataUnit.class));
    }
}
