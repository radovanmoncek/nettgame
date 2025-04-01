package cz.radovanmoncek.test.suite;

import cz.radovanmoncek.test.modules.games.codecs.GameStateFlatBuffersEncoderTest;
import cz.radovanmoncek.test.modules.games.codecs.RequestFlatBuffersDecoderTest;
import cz.radovanmoncek.test.modules.games.handlers.ExampleGameSessionChannelGroupHandlerTest;
import cz.radovanmoncek.test.modules.games.models.GameHistoryEntityTest;
import cz.radovanmoncek.test.modules.games.models.GameStateFlatBuffersSerializableTest;
import cz.radovanmoncek.test.ship.creators.ExampleGameSessionHandlerCreatorTest;
import cz.radovanmoncek.test.ship.creators.GameStateFlatBuffersEncoderCreatorTest;
import cz.radovanmoncek.test.ship.creators.RequestFlatBuffersDecoderCreatorTest;
import cz.radovanmoncek.test.ship.launcher.NettgameServerLauncherTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Example nettgame server test suite")
@SelectClasses({
        NettgameServerLauncherTest.class,
        ExampleGameSessionHandlerCreatorTest.class,
        GameStateFlatBuffersEncoderCreatorTest.class,
        RequestFlatBuffersDecoderCreatorTest.class,
        GameStateFlatBuffersSerializableTest.class,
        GameHistoryEntityTest.class,
            GameStateFlatBuffersEncoderTest.class,
            RequestFlatBuffersDecoderTest.class,
            ExampleGameSessionChannelGroupHandlerTest.class
    })
    public class NettgameServerTest {
}
