package client.modules.sessions.handlers;

import client.ship.bootstrap.GameClient;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.game.docker.modules.session.pdus.SessionPDU;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionClientHandlerTest {
    private static GameClient gameClient;
    private static SessionClientHandler sessionClientHandler;
    private static byte authoritativeSessionFlag;

    @BeforeAll
    static void setup() throws Exception {
        (gameClient = GameClient.newInstance())
                .withSessionClientHandlerSupplier(() -> sessionClientHandler = new SessionClientHandler(){

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, SessionPDU msg) {
                        authoritativeSessionFlag = msg.sessionFlag();
                    }
                })
                .run(1, 5);
    }

    @Test
    void startSessionTest() throws InterruptedException {
        sessionClientHandler.requestStartSession();

        TimeUnit.MILLISECONDS.sleep(500);

        assertEquals(SessionPDU.SessionFlag.START.ordinal(), authoritativeSessionFlag);
    }
}
