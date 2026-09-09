package cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.codecs;

import com.google.flatbuffers.Table;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FlatBuffersDecoderTest {
    private static EmbeddedChannel channel;
    private static FlatBuffersDecoder<?> decoder;
    private static byte decodedHeader;
    private static ByteBuffer decoded;

    @BeforeEach
    void setUp() {

        decoder = new FlatBuffersDecoder<>() {
            @Override
            protected boolean decodeHeader(ByteBuffer in) {
                return in.get() == (decodedHeader = 't');
            }

            @Override
            protected Table decodeBodyAfterHeader(ByteBuffer in) {

                decoded = in;

                return new Table();
            }
        };
        channel = new EmbeddedChannel(decoder);
    }

    @Test
    void decode() throws InterruptedException {

        final var length = ByteBuffer.allocate(8).putLong(100L).position(0).array();
        final var body = new byte[]{'t', 0x08, 0x00, 0x0c, 0x00, 0x08, 0x00, 0x04, 0x00,
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

        channel.writeInbound(Unpooled.wrappedBuffer(length, body));

        TimeUnit.MILLISECONDS.sleep(50);

        channel.advanceTimeBy(50, TimeUnit.MILLISECONDS);

        final var result = (Table) channel.readInbound();

        assertNotNull(result);

        assertEquals('t', decodedHeader);

        assertNotNull(decoded);
    }
}
