package cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.nettgame.nettgame.ship.deck.models.FlatBufferSerializable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FlatBuffersEncoderTest {
    private static FlatBuffersEncoder<FlatBufferSerializable> encoder;
    private static EmbeddedChannel channel;
    private static byte [] byteArray;

    @BeforeEach
    void setUp() {

        encoder = new FlatBuffersEncoder<>() {
            @Override
            protected byte[] encodeHeader(FlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {

                return new byte[]{0x30};
            }

            @Override
            protected byte[] encodeBodyAfterHeader(FlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {

                return byteArray = new byte[]{
                        0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x08, 0x00, 0x0c, 0x00, 0x08, 0x00, 0x04, 0x00,
                        0x08, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x5c,
                        0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x2c, 0x00
                       , 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, (byte) 0xe4, (byte) 0xff, (byte) 0xff
                        , (byte) 0xff, 0x00, 0x00, 0x16, 0x43, 0x00, 0x00, (byte) 0x84, 0x43
                       , 0x0e, 0x01, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x03
                       , 0x00, 0x00, 0x00, 0x6d, 0x61, 0x74, 0x00, 0x08, 0x00,
                        0x14, 0x00, 0x04, 0x00, 0x10, 0x00, 0x08, 0x00, 0x00
                       , 0x00, 0x00, 0x00, (byte) 0xa3, 0x43, 0x00, 0x00, (byte) 0x85, 0x43,
                        0x5a, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x03
                       , 0x00, 0x00, 0x00, 0x70, 0x61, 0x74, 0x00, 0x00, 0x00
                       , 0x0a, 0x00, 0x14, 0x00, 0x07, 0x00, 0x08, 0x00, 0x0c
                       , 0x00, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03
                       , 0x0c, 0x00, 0x00, 0x00, (byte) 0xac, 0x20, 0x03, 0x00, 0x00
                       , 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
                       , 0x00, 0x00
                };
            }
        };
        channel = new EmbeddedChannel(new LoggingHandler(LogLevel.INFO), encoder);
    }

    @Test
    void encode() throws InterruptedException {

        final var serializable = new FlatBufferSerializable(){

            @Override
            public byte[] serialize(FlatBufferBuilder builder) {
                return new byte[0];
            }
        };

        channel.writeOutbound(serializable);
        channel.advanceTimeBy(10, TimeUnit.MILLISECONDS);

        TimeUnit.MILLISECONDS.sleep(10);

        final var result = (ByteBuf) channel.readOutbound();

        assertNotNull(result);

        assertEquals(byteArray.length + 1, result.readLong());
        assertEquals(0x30, result.readByte());

        for (var i = 0; i < result.readableBytes(); i++) {

            assertEquals(byteArray[i], result.readByte());
        }
    }
}
