package cz.radovanmoncek.test.ship.creators;

import cz.radovanmoncek.server.modules.games.codecs.GameStateRequestFlatBuffersDecoder;
import cz.radovanmoncek.server.ship.creators.GameStateRequestFlatBuffersDecoderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateRequestFlatBuffersDecoderCreatorTest {

    @Test
    void newProduct() {

        assertInstanceOf(GameStateRequestFlatBuffersDecoder.class, new GameStateRequestFlatBuffersDecoderCreator().newProduct());
    }
}