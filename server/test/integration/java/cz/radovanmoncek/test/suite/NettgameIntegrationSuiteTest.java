package cz.radovanmoncek.test.suite;

import cz.radovanmoncek.test.ship.injection.services.ChannelHandlerInjectionServiceTest;
import cz.radovanmoncek.test.ship.sessions.events.DefaultGameSessionContextTest;
import cz.radovanmoncek.test.ship.sessions.events.GameSessionEventLoopTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("lslslsl")
@SelectClasses({
        DefaultGameSessionContextTest.class,
        GameSessionEventLoopTest.class,
        ChannelHandlerInjectionServiceTest.class
})
public class NettgameIntegrationSuiteTest {
}
