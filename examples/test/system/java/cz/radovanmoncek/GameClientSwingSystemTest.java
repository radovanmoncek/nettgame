package cz.radovanmoncek;

import cz.radovanmoncek.client.ship.bootstrap.ExampleGameClient;
import org.junit.jupiter.api.BeforeAll;

import java.awt.*;

public class GameClientSwingSystemTest {
    private static Robot robot;

    @BeforeAll
    static void setup() throws AWTException {

        ExampleGameClient.main(null);

        robot = new Robot();
    }
}
