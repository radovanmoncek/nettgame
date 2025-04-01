package cz.radovanmoncek.test.ship.bootstrap;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.bootstrap.NettgameServerBootstrap;
import cz.radovanmoncek.ship.bootstrap.exception.BootstrapInvalidException;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersDecoder;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.handlers.ChannelGroupHandler;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class NettgameServerBootstrapTest {
    private static NettgameServerBootstrap nettgameServerBootstrap;

    @BeforeAll
    static void setup() {

        nettgameServerBootstrap = NettgameServerBootstrap.returnNewInstance();
    }

    @Test
    void singletonTest() throws Exception {

        final var instanceField = nettgameServerBootstrap.getClass().getDeclaredField("instance");

        instanceField.setAccessible(true);

        final var instance = (NettgameServerBootstrap) instanceField.get(nettgameServerBootstrap);

        assertNotNull(instance);

        assertEquals(instance, nettgameServerBootstrap);
        assertEquals(instance, NettgameServerBootstrap.returnNewInstance());
    }

    @Test
    void addChannelHandlerCreatorTest() throws Exception {

        ChannelHandlerCreator channelHandlerCreator;

        nettgameServerBootstrap.addChannelHandlerCreator(channelHandlerCreator = new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new ChannelGroupHandler<>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Table msg) {}
                };
            }
        });

        final var instanceField = nettgameServerBootstrap
                .getClass()
                .getDeclaredField("channelHandlerCreators");

        instanceField.setAccessible(true);

        final var channelGroupHandlerCreators = (LinkedList<?>) instanceField.get(nettgameServerBootstrap);

        assertEquals(1, channelGroupHandlerCreators.size());
        assertEquals(channelHandlerCreator, channelGroupHandlerCreators.getFirst());
        assertEquals(ChannelGroupHandler.class, ((ChannelHandlerCreator) channelGroupHandlerCreators.getFirst()).newProduct().getClass().getSuperclass());

        nettgameServerBootstrap.addChannelHandlerCreator(new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new FlatBuffersDecoder<>() {

                    @Override
                    protected boolean decodeHeader(ByteBuffer in) {
                        return false;
                    }

                    @Override
                    protected Table decodeBodyAfterHeader(ByteBuffer in) {

                        return null;
                    }
                };
            }
        });

        assertEquals(2, channelGroupHandlerCreators.size());

        final var decoder = (FlatBuffersDecoder<?>) ((ChannelHandlerCreator) channelGroupHandlerCreators.getLast()).newProduct();

        assertNotNull(decoder);

        nettgameServerBootstrap.addChannelHandlerCreator(new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new FlatBuffersEncoder<>() {
                    @Override
                    protected byte[] encodeHeader(FlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {
                        return new byte[0];
                    }

                    @Override
                    protected byte[] encodeBodyAfterHeader(FlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {
                        return new byte[0];
                    }
                };
            }
        });

        assertEquals(3, channelGroupHandlerCreators.size());

        final var encoder = (FlatBuffersEncoder<?>) ((ChannelHandlerCreator) channelGroupHandlerCreators.getLast()).newProduct();

        assertNotNull(encoder);
    }

    @Test
    void setPortTest() throws NoSuchFieldException, IllegalAccessException {

        nettgameServerBootstrap.setPort(4321);

        final var setPort = ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrap, "port");

        assertEquals(4321, setPort);
    }

    @Test
    void setAddress() throws NoSuchFieldException, IllegalAccessException {

        nettgameServerBootstrap.setInternetProtocolAddress(InetAddress.getLoopbackAddress());

        final var setAddress = (InetAddress) ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrap, "address");

        assertEquals("127.0.0.1", setAddress.getHostAddress());
    }

    @Test
    void setLogLevel() throws NoSuchFieldException, IllegalAccessException {

        nettgameServerBootstrap.setLogLevel(LogLevel.INFO);

        final var setField = (LinkedList<?>) ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrap, "initialHandlers");

        assertEquals(LoggingHandler.class, setField.getFirst().getClass());
    }

    @Test
    void setShutdownTimeout() throws NoSuchFieldException, IllegalAccessException {

        nettgameServerBootstrap.setShutdownTimeout(1000);

        final var setField = ReflectionUtilities.returnValueOnFieldReflectively(nettgameServerBootstrap, "shutdownTimeout");

        assertEquals(1000, setField);
    }

    @Test
    void runTest() throws NoSuchFieldException, IllegalAccessException {

        ReflectionUtilities.setValueOnFieldReflectively(nettgameServerBootstrap, "instance", null);

        final var newInstance = NettgameServerBootstrap.returnNewInstance();

        newInstance.setInternetProtocolAddress(null);

        assertThrows(BootstrapInvalidException.class, newInstance::run);
    }
}
