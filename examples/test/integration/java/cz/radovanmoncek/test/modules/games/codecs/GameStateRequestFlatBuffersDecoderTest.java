package cz.radovanmoncek.test.modules.games.codecs;

import cz.radovanmoncek.server.modules.games.codecs.GameStateRequestFlatBuffersDecoder;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class GameStateRequestFlatBuffersDecoderTest {
    private static GameStateRequestFlatBuffersDecoder decoder;

    @BeforeAll
    static void setup() {

        decoder = new GameStateRequestFlatBuffersDecoder();
    }

    @Test
    void decodeHeader() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        ReflectionUtilities.invokeNonPublicMethod(decoder, "decodeHeader", new Class[]{ByteBuffer.class}, new Object[]{ByteBuffer.wrap(new byte[]{'g'})});
    }

    @Test
    void decodeBodyAfterHeader() {
    }
}