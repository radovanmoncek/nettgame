package container.game.docker.ship.bootstrap;

import com.google.flatbuffers.FlatBufferBuilder;
import container.game.docker.ship.parents.codecs.FlatBuffersDecoder;
import container.game.docker.ship.parents.codecs.FlatBuffersEncoder;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.FlatBufferSerializable;
import container.game.docker.ship.parents.products.Product;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstanceContainerBootstrapBareMetalTest {
    private static InstanceContainerBootstrap instanceContainerBootstrap;

    @BeforeAll
    static void setup() {

        instanceContainerBootstrap = InstanceContainerBootstrap.newInstance();
    }

    @Test
    void singletonTest() throws Exception {

        final var instanceField = instanceContainerBootstrap.getClass().getDeclaredField("instance");

        instanceField.setAccessible(true);

        final var instance = (InstanceContainerBootstrap) instanceField.get(instanceContainerBootstrap);

        assertNotNull(instance);

        assertEquals(instance, instanceContainerBootstrap);
        assertEquals(instance, InstanceContainerBootstrap.newInstance());
    }

    @Test
    void handlerFactoryAdditionTest() throws Exception {

        Creator creator;

        instanceContainerBootstrap.addChannelHandlerCreator(creator = new Creator() {

            @Override
            public Product newProduct() {

                return new ChannelGroupHandler<>() {

                    @Override
                    protected void playerChannelRead(Object schema, Channel playerChannel) {

                    }

                    @Override
                    protected void playerDisconnected(Channel playerChannel) {

                    }
                };
            }
        });

        final var instanceField = instanceContainerBootstrap.getClass().getDeclaredField("channelGroupHandlerSuppliers");

        instanceField.setAccessible(true);

        final var channelGroupHandlerSuppliers = (LinkedList<?>) instanceField.get(instanceContainerBootstrap);

        assertEquals(1, channelGroupHandlerSuppliers.size());
        assertEquals(creator, channelGroupHandlerSuppliers.getFirst());
        assertEquals(ChannelGroupHandler.class, ((Creator) channelGroupHandlerSuppliers.getFirst()).newProduct().getClass().getSuperclass());

        instanceContainerBootstrap.addChannelHandlerCreator(new Creator() {

            @Override
            public Product newProduct() {

                return new FlatBuffersDecoder<>(Object.class) {

                    @Override
                    protected Object decodeBodyAfterHeader(ByteBuffer in) {

                        return null;
                    }
                };
            }
        });

        assertEquals(2, channelGroupHandlerSuppliers.size());

        final var decoder = (FlatBuffersDecoder<?>) ((Creator) channelGroupHandlerSuppliers.getLast()).newProduct();
        final var bindingsField = decoder.getClass().getSuperclass().getDeclaredField("protocolIdentifierToProtocolDataUnitBindings");

        bindingsField.setAccessible(true);

        assertNotNull(bindingsField.get(decoder));

        assertEquals(HashMap.class, bindingsField.get(decoder).getClass());

        instanceContainerBootstrap.registerMagicByteToFlatBufferSerializableBinding((byte) 2, FlatBufferSerializable.class);
        instanceContainerBootstrap.addChannelHandlerCreator(new Creator() {

            @Override
            public Product newProduct() {

                return new FlatBuffersEncoder<>() {

                    @Override
                    protected byte [] encodeBodyAfterHeader(FlatBufferSerializable<?> flatBufferSerializable, FlatBufferBuilder builder) {

                        return null;
                    }
                };
            }
        });

        assertEquals(3, channelGroupHandlerSuppliers.size());

        final var encoder = (FlatBuffersEncoder<?>) ((Creator) channelGroupHandlerSuppliers.getLast()).newProduct();
        final var bindingsFieldEncoder = encoder.getClass().getSuperclass().getDeclaredField("protocolDataUnitToProtocolIdentifierBindings");

        bindingsFieldEncoder.setAccessible(true);

        assertNotNull(bindingsFieldEncoder.get(encoder));

        assertEquals(HashMap.class, bindingsFieldEncoder.get(encoder).getClass());
        assertEquals((byte) 2, ((HashMap<?, ?>) bindingsFieldEncoder.get(encoder)).get(FlatBufferSerializable.class));
    }
}
