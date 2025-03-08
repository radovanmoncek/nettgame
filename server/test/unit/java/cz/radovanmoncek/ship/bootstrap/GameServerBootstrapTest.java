package cz.radovanmoncek.ship.bootstrap;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersDecoder;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.handlers.ChannelGroupHandler;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameServerBootstrapTest {
    private static GameServerBootstrap gameServerBootstrap;

    @BeforeAll
    static void setup() {

        gameServerBootstrap = GameServerBootstrap.newInstance();
    }

    @Test
    void singletonTest() throws Exception {

        final var instanceField = gameServerBootstrap.getClass().getDeclaredField("instance");

        instanceField.setAccessible(true);

        final var instance = (GameServerBootstrap) instanceField.get(gameServerBootstrap);

        assertNotNull(instance);

        assertEquals(instance, gameServerBootstrap);
        assertEquals(instance, GameServerBootstrap.newInstance());
    }

    @Test
    void handlerFactoryAdditionTest() throws Exception {

        ChannelHandlerCreator channelHandlerCreator;

        gameServerBootstrap.addChannelHandlerCreator(channelHandlerCreator = new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new ChannelGroupHandler<>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Table msg) throws Exception {

                    }
                };
            }
        });

        final var instanceField = gameServerBootstrap.getClass().getDeclaredField("channelGroupHandlerSuppliers");

        instanceField.setAccessible(true);

        final var channelGroupHandlerSuppliers = (LinkedList<?>) instanceField.get(gameServerBootstrap);

        assertEquals(1, channelGroupHandlerSuppliers.size());
        assertEquals(channelHandlerCreator, channelGroupHandlerSuppliers.getFirst());
        assertEquals(ChannelGroupHandler.class, ((ChannelHandlerCreator) channelGroupHandlerSuppliers.getFirst()).newProduct().getClass().getSuperclass());

        gameServerBootstrap.addChannelHandlerCreator(new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new FlatBuffersDecoder<>(Table.class) {

                    @Override
                    protected Table decodeBodyAfterHeader(ByteBuffer in) {

                        return null;
                    }
                };
            }
        });

        assertEquals(2, channelGroupHandlerSuppliers.size());

        final var decoder = (FlatBuffersDecoder<?>) ((ChannelHandlerCreator) channelGroupHandlerSuppliers.getLast()).newProduct();
        final var bindingsField = decoder.getClass().getSuperclass().getDeclaredField("protocolIdentifierToProtocolDataUnitBindings");

        bindingsField.setAccessible(true);

        assertNotNull(bindingsField.get(decoder));

        assertEquals(HashMap.class, bindingsField.get(decoder).getClass());

        gameServerBootstrap.registerMagicByteToFlatBufferSerializableBinding((byte) 2, FlatBufferSerializable.class);
        gameServerBootstrap.addChannelHandlerCreator(new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new FlatBuffersEncoder<>() {

                    @Override
                    protected byte [] encodeBodyAfterHeader(FlatBufferSerializable<?> flatBufferSerializable, FlatBufferBuilder builder) {

                        return null;
                    }
                };
            }
        });

        assertEquals(3, channelGroupHandlerSuppliers.size());

        final var encoder = (FlatBuffersEncoder<?>) ((ChannelHandlerCreator) channelGroupHandlerSuppliers.getLast()).newProduct();
        final var bindingsFieldEncoder = encoder.getClass().getSuperclass().getDeclaredField("protocolDataUnitToProtocolIdentifierBindings");

        bindingsFieldEncoder.setAccessible(true);

        assertNotNull(bindingsFieldEncoder.get(encoder));

        assertEquals(HashMap.class, bindingsFieldEncoder.get(encoder).getClass());
        assertEquals((byte) 2, ((HashMap<?, ?>) bindingsFieldEncoder.get(encoder)).get(FlatBufferSerializable.class));
    }
}
