package cz.radovanmoncek.test.ship.launcher;

import cz.radovanmoncek.ship.launcher.NettgameServerLauncher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NettgameServerLauncherTest {

    @Test
    void mainTest() {

        assertThrows(NullPointerException.class, () -> NettgameServerLauncher.main(null));
    }
}
