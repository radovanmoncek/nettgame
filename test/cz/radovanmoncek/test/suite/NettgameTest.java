package cz.radovanmoncek.test.suite;

import cz.radovanmoncek.test.ship.bootstrap.NettgameServerBootstrapTest;
import cz.radovanmoncek.test.ship.builders.NettgameServerBootstrapBuilderTest;
import cz.radovanmoncek.test.ship.directors.NettgameServerBootstrapDirectorTest;
import cz.radovanmoncek.test.ship.injection.services.ChannelHandlerInjectionServiceTest;
import cz.radovanmoncek.test.ship.events.DefaultGameSessionContextTest;
import cz.radovanmoncek.test.ship.events.GameSessionEventLoopTest;
import cz.radovanmoncek.test.ship.parents.codecs.FlatBuffersEncoderTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("lslslsl")
@SelectClasses({
        DefaultGameSessionContextTest.class,
        GameSessionEventLoopTest.class,
        ChannelHandlerInjectionServiceTest.class,
        NettgameServerBootstrapTest.class,
        NettgameServerBootstrapBuilderTest.class,
        NettgameServerBootstrapDirectorTest.class,
        FlatBuffersEncoderTest.class
    })
public class NettgameTest {
}
