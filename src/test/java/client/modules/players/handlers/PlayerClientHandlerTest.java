package client.modules.players.handlers;

import client.modules.player.handlers.PlayerClientHandler;
import client.ship.bootstrap.GameClient;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.game.docker.modules.player.pdus.NicknamePDU;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerClientHandlerTest {
    private static GameClient gameClient;
    private static PlayerClientHandler playerClientHandler;
    private static String authoritativeNickname;

    @BeforeAll
    static void setup() throws Exception {
        (gameClient = GameClient.newInstance())
                .withPlayerClientHandlerSupplier(() -> playerClientHandler = new PlayerClientHandler(){

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, NicknamePDU msg) {
                        authoritativeNickname = msg.nickname();
                    }
                })
                .run(1, 5);
    }

    @Test
    void nicknameRequestTest() throws InterruptedException {
        playerClientHandler.requestNickname("Test");

        TimeUnit.MILLISECONDS.sleep(500);

        assertEquals("Test", authoritativeNickname);
    }

    @Test
    void tooLongNicknameRequest() {
        assertThrows(IllegalArgumentException.class, () -> playerClientHandler.requestNickname("VeryLongNicknameThatIsOverEightCharacters"));
    }

    @AfterAll
    static void tearDown() throws Exception {
        gameClient.shutdownGracefullyAfterNSeconds(0);
    }
}
