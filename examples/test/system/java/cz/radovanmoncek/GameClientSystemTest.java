package cz.radovanmoncek;

import cz.radovanmoncek.client.ship.bootstrap.ExampleGameClient;
import org.junit.jupiter.api.BeforeAll;

public class GameClientSystemTest {

    @BeforeAll
    static void setup(){

        ExampleGameClient.main(null);
    }
}
