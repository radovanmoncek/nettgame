package cz.radovanmoncek.test.suite;

import cz.radovanmoncek.test.modules.games.codecs.GameStateFlatBuffersEncoderTest;
import cz.radovanmoncek.test.modules.games.codecs.GameStateRequestFlatBuffersDecoderTest;
import cz.radovanmoncek.test.modules.games.handlers.ExampleGameSessionChannelGroupHandlerTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Integration")
@SelectClasses({
        GameStateFlatBuffersEncoderTest.class,
        GameStateRequestFlatBuffersDecoderTest.class,
        ExampleGameSessionChannelGroupHandlerTest.class
})
public class ExampleNettgameServerIntegrationTestSuite {
}
