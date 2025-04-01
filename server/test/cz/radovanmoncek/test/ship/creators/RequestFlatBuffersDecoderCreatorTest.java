package cz.radovanmoncek.test.ship.creators;

import cz.radovanmoncek.modules.games.codecs.RequestFlatBuffersDecoder;
import cz.radovanmoncek.ship.creators.GameStateRequestFlatBuffersDecoderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RequestFlatBuffersDecoderCreatorTest {

    @Test
    void newProduct() {

        assertInstanceOf(RequestFlatBuffersDecoder.class, new GameStateRequestFlatBuffersDecoderCreator().newProduct());
    }
}