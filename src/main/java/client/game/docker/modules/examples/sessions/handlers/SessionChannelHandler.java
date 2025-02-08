package client.game.docker.modules.examples.sessions.handlers;

import client.game.docker.ship.bootstrap.examples.SampleGameClient;
import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.session.models.SessionFlag;
import container.game.docker.modules.examples.session.models.SessionResponseProtocolDataUnit;
import container.game.docker.modules.examples.session.models.SessionRequestProtocolDataUnit;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SessionChannelHandler extends ChannelHandler<SessionResponseProtocolDataUnit, SessionRequestProtocolDataUnit> {
    private final SampleGameClient client;
    private String confirmedNickname, nicknameTemporaryBuffer = "", sessionCodeTemporaryBuffer = "";

    public SessionChannelHandler(final SampleGameClient client) {

        this.client = client;

        client.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {

                if(e.getKeyCode() == KeyEvent.VK_ENTER) {

                    if(confirmedNickname == null) {

                        confirmedNickname = nicknameTemporaryBuffer;

                        return;
                    }

                    unicastToServerChannel(new SessionRequestProtocolDataUnit(SessionFlag.START, 0, 0, 0, 0, confirmedNickname));
                }

                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if(!sessionCodeTemporaryBuffer.isEmpty())
                        sessionCodeTemporaryBuffer = sessionCodeTemporaryBuffer.substring(0, sessionCodeTemporaryBuffer.length() - 1);

                    if(!nicknameTemporaryBuffer.isEmpty())
                        nicknameTemporaryBuffer = nicknameTemporaryBuffer.substring(0, nicknameTemporaryBuffer.length() - 1);

                    return;
                }

                if((confirmedNickname == null && nicknameTemporaryBuffer.length() > 8) || sessionCodeTemporaryBuffer.length() > 10 || !String.copyValueOf(new char[] {e.getKeyChar()}).matches("[a-z]"))
                    return;

                if(confirmedNickname == null) {

                    nicknameTemporaryBuffer += e.getKeyChar();

                    return;
                }

                sessionCodeTemporaryBuffer += e.getKeyChar();
            }
        });

        client.setOnPaint(graphics -> {

            graphics.setColor(Color.WHITE);

            if(confirmedNickname == null) {

                graphics.drawString(nicknameTemporaryBuffer.isBlank() ? "Please enter your nickname" : nicknameTemporaryBuffer, client.getWidth() / 2 - 8 * 3, client.getHeight() / 2);

                return;
            }

            graphics.drawString(sessionCodeTemporaryBuffer.isBlank() ? "Please enter lobby code" : sessionCodeTemporaryBuffer, client.getWidth() / 2 - sessionCodeTemporaryBuffer.length() * 3, client.getHeight() / 2);
        });
    }

    @Override
    protected void serverChannelRead(final SessionResponseProtocolDataUnit protocolDataUnit) {

        System.out.printf("Session response received from the server %s\n", protocolDataUnit); //todo: log4j
    }
}
