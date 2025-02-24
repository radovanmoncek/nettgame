package container.game.docker.modules.examples.lobbies.handlers;

import client.game.docker.modules.examples.lobbies.handlers.LobbyChannelHandler;
import client.game.docker.ship.bootstrap.GameClient;
import client.game.docker.ship.bootstrap.examples.SampleGameClient;
import container.game.docker.modules.examples.lobbies.models.LobbyFlag;
import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LobbyChannelGroupHandlerTest {
    private static GameClient gameClient;
    private static LobbyResponseProtocolDataUnit lobbyResponseProtocolDataUnit;
    private static Method unicastToServerChannel;

    @BeforeAll
    static void setup() {

        (gameClient = GameClient.newInstance())
                .withChannelHandler(new LobbyChannelHandler() {

                    {

                        try {

                            LobbyChannelGroupHandlerTest.unicastToServerChannel = getClass().getSuperclass().getMethod("unicastToServerChannel");
                        } catch (NoSuchMethodException e) {

                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    protected void serverChannelRead(final LobbyResponseProtocolDataUnit msg) {

                        lobbyResponseProtocolDataUnit = msg;
                    }
                })
                .run();
    }

    @Test
    void createLobbyTest() throws InterruptedException {

//        unicastToServerChannel.invoke();

        TimeUnit.MILLISECONDS.sleep(500);

        assertEquals(LobbyFlag.CREATE, lobbyResponseProtocolDataUnit.lobbyFlag());
    }

    @AfterAll
    static void tearDown() {

        gameClient.shutdownGracefullyAfterNSeconds(0);
    }
}
