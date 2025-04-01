package cz.radovanmoncek.test.ship.creators;

import cz.radovanmoncek.modules.games.codecs.GameStateFlatBuffersEncoder;
import cz.radovanmoncek.ship.creators.GameStateFlatBuffersEncoderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateFlatBuffersEncoderCreatorTest {

    @Test
    void newProduct() {

        assertInstanceOf(GameStateFlatBuffersEncoder.class, new GameStateFlatBuffersEncoderCreator().newProduct());
    }
}