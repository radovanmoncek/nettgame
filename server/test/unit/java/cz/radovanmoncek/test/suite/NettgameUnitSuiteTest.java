package cz.radovanmoncek.test.suite;

import cz.radovanmoncek.test.ship.bootstrap.NettgameServerBootstrapTest;
import cz.radovanmoncek.test.ship.builders.NettgameServerBootstrapBuilderTest;
import cz.radovanmoncek.test.ship.directors.NettgameServerBootstrapDirectorTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("\uD83D\uDEA2 Game Server Ship Layer Test Suite")
@SelectClasses({
        NettgameServerBootstrapTest.class,
        NettgameServerBootstrapBuilderTest.class,
        NettgameServerBootstrapDirectorTest.class
})
public class NettgameUnitSuiteTest {
}
