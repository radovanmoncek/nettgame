package client.game.docker.ship.bootstrap.examples;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SampleGameClientTest {
    private static SampleGameClient sampleGameClient;

    @BeforeAll
    static void setup() throws Exception {

        sampleGameClient = new SampleGameClient();
    }

    @Test
    void gameClientNotNullTest() throws Exception {

        final var gameClientField = sampleGameClient.getClass().getDeclaredField("gameClient");

        gameClientField.setAccessible(true);

        assertNotNull(gameClientField.get(sampleGameClient));
    }
}
