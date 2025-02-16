package client.game.docker.modules.examples.sessions.handlers;

import client.game.docker.ship.bootstrap.examples.SampleGameClient;
import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SessionChannelHandler extends ChannelHandler<SessionResponseProtocolDataUnit, SessionRequestProtocolDataUnit> {
    private static final Logger logger = LogManager.getLogger(SessionChannelHandler.class);
    private final SampleGameClient client;
    private ClientState clientState;
    private int x1, x2, rotationAngle1, y1, y2, rotationAngle2;
    private boolean sessionHost = false;

    public SessionChannelHandler(final SampleGameClient client) {

        this.client = client;

        client.setOnPaint(graphics -> clientState.render((Graphics2D) graphics));

        client.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent keyEvent) {

                clientState.onKeyPress(keyEvent);
            }
        });

        clientState = new MainMenuClientState();
    }

    @Override
    protected void serverChannelRead(final SessionResponseProtocolDataUnit protocolDataUnit) {

        switch (protocolDataUnit.sessionFlag()) {

            case START -> {

                sessionHost = true;

                clientState = new ClientState() {

                    @Override
                    public void render(final Graphics2D graphics) {

                        graphics.setColor(Color.WHITE);

                        graphics.drawString(protocolDataUnit.sessionUUID(), client.getWidth() / 2, 20);

                        graphics.drawString(protocolDataUnit.nickname1(), x1, y1 - 4);

                        graphics.rotate(Math.toRadians(rotationAngle1), x1 + 10, y1 + 10);

                        graphics.translate(x1, y1);

                        graphics.fillPolygon(new int[]{20, 30, 40}, new int[] {20, 10, 20}, 3);
                    }

                    public void onKeyPress(final KeyEvent keyEvent) {

                        final var x = x1;
                        final var y = y1;

                        sendPlayerInput(keyEvent, x, y);
                    }
                };
            }

            case JOIN ->

                clientState = new ClientState() {

                    @Override
                    public void render(final Graphics2D graphics) {

                        graphics.setColor(Color.WHITE);

                        graphics.drawString(protocolDataUnit.sessionUUID(), client.getWidth() / 2, 20);

                        graphics.drawString(protocolDataUnit.nickname1(), x1, y1 - 4);

                        graphics.rotate(Math.toRadians(rotationAngle1));

                        graphics.fillRect(x1, y1, 20, 20);

                        graphics.rotate(- Math.toRadians(rotationAngle1));

                        graphics.drawString(protocolDataUnit.nickname2(), x2, y2 - 4);

                        graphics.rotate(Math.toRadians(rotationAngle2));

                        graphics.fillRect(x2, y2, 20, 20);

                        graphics.rotate(- Math.toRadians(rotationAngle2));
                    }

                    public void onKeyPress(final KeyEvent keyEvent) {

                        final var x = sessionHost ? x1 : x2;
                        final var y = sessionHost ? y1 : y2;

                        sendPlayerInput(keyEvent, x, y);
                    }
                };

            case STATE -> { //todo: proper non-naive implementation (client-side prediction)

                x1 = protocolDataUnit.x1();
                y1 = protocolDataUnit.y1();
                rotationAngle1 = protocolDataUnit.rotationAngle1();
                x2 = protocolDataUnit.x2();
                y2 = protocolDataUnit.y2();
                rotationAngle2 = protocolDataUnit.rotationAngle2();
            }
        }

        logger.info("Session response received from the server {}", protocolDataUnit);
    }

    private void sendPlayerInput(final KeyEvent keyEvent, int x, int y) {

        switch (keyEvent.getKeyCode()) {

            case KeyEvent.VK_W -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x,
                    y - 4,
                    0
            ));

            case KeyEvent.VK_A -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x - 4,
                    y,
                    270
            ));

            case KeyEvent.VK_S -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x,
                    y + 4,
                    180
            ));

            case KeyEvent.VK_D -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x + 4,
                    y,
                    90
            ));
        }
    }

    private interface ClientState {

        void render(final Graphics2D graphics);

        void onKeyPress(final KeyEvent keyCode);
    }

    private class MainMenuClientState implements ClientState {
        private boolean newSessionSelected = true, joinSessionSelected = false;

        @Override
        public void render(Graphics2D graphics) {

            graphics.setColor(Color.WHITE);

            if (newSessionSelected)
                graphics.setColor(Color.YELLOW);

            final var newSessionText = "New Session";

            graphics
                    .drawString(
                            newSessionText,
                            client.getWidth() / 2 - graphics.getFontMetrics().stringWidth(newSessionText) / 2,
                            client.getHeight() / 2 - graphics.getFontMetrics().getHeight() / 2
                    );

            graphics.setColor(Color.WHITE);

            if (joinSessionSelected)
                graphics.setColor(Color.YELLOW);

            final var joinSessionText = "Join Session";

            graphics.drawString(
                    joinSessionText,
                    client.getWidth() / 2 - graphics.getFontMetrics().stringWidth(joinSessionText) / 2,
                    client.getHeight() / 2 + graphics.getFontMetrics().getHeight() / 2
            );
        }

        @Override
        public void onKeyPress(KeyEvent keyEvent) {

            switch (keyEvent.getKeyCode()) {

                case KeyEvent.VK_ENTER -> clientState = newSessionSelected ?
                        new NewSessionSelectedClientState() :
                        new JoinSessionSelectedClientState();

                case KeyEvent.VK_W -> {

                    newSessionSelected = true;
                    joinSessionSelected = false;
                }

                case KeyEvent.VK_S -> {

                    newSessionSelected = false;
                    joinSessionSelected = true;
                }
            }
        }
    }

    private class NewSessionSelectedClientState implements ClientState {
        private final StringBuilder userInput = new StringBuilder();

        @Override
        public void render(final Graphics2D graphics) {

            graphics.setColor(Color.BLUE);

            graphics.setFont(new Font("Arial", Font.BOLD, 12));

            graphics.drawRect(
                    client.getWidth() / 2 - client.getWidth() / 8,
                    client.getHeight() / 2 - client.getHeight() / 16,
                    client.getWidth() / 4,
                    client.getHeight() / 8
            );

            if (userInput.isEmpty()) {

                graphics.drawString("Please enter your nickname", client.getWidth() / 2, client.getHeight() / 2);

                return;
            }

            graphics.drawString(userInput.toString(), client.getWidth() / 2, client.getHeight() / 2);
        }

        @Override
        public void onKeyPress(KeyEvent keyEvent) {

            switch (keyEvent.getKeyCode()) {

                case KeyEvent.VK_ENTER -> {

                    if (userInput.isEmpty())
                        return;

                    unicastToServerChannel(SessionRequestProtocolDataUnit.newSTART(userInput.toString()));
                }

                case KeyEvent.VK_BACK_SPACE -> {

                    if (userInput.isEmpty())
                        return;

                    userInput.deleteCharAt(userInput.length() - 1);
                }

                default -> {

                    if (userInput.length() > 8 || !String.copyValueOf(new char[]{keyEvent.getKeyChar()}).matches("[a-z0-9]"))
                        return;

                    userInput.append(keyEvent.getKeyChar());
                }
            }
        }
    }

    private class JoinSessionSelectedClientState implements ClientState {
        private final StringBuilder userInput = new StringBuilder();
        private String nickname;

        @Override
        public void render(final Graphics2D graphics) {

            graphics.setColor(Color.BLUE);

            graphics.setFont(new Font("Arial", Font.PLAIN, 12));

            graphics.drawRect(
                    client.getWidth() / 2 - client.getWidth() / 8,
                    client.getHeight() / 2 - client.getHeight() / 16,
                    client.getWidth() / 4,
                    client.getHeight() / 8
            );

            if (userInput.isEmpty()) {

                final var text = nickname == null ? "Please enter your nickname" : "Please enter session code";

                graphics
                        .drawString(
                                text,
                                client.getWidth() / 2 - graphics.getFontMetrics().stringWidth(text) / 2,
                                client.getHeight() / 2 - graphics.getFontMetrics().getHeight() / 2
                        );

                return;
            }

            graphics.drawString(userInput.toString(), client.getWidth() / 2, client.getHeight() / 2);
        }

        @Override
        public void onKeyPress(final KeyEvent keyEvent) {

            switch (keyEvent.getKeyCode()) {

                case KeyEvent.VK_ENTER -> {

                    if (userInput.isEmpty())
                        return;

                    if (nickname == null) {

                        nickname = userInput.toString();

                        userInput.delete(0, userInput.length());

                        return;
                    }

                    if (userInput.length() != 8)
                        return;

                    unicastToServerChannel(SessionRequestProtocolDataUnit.newJOIN(nickname, userInput.toString()));
                }

                case KeyEvent.VK_BACK_SPACE -> {

                    if (userInput.isEmpty())
                        return;

                    userInput.deleteCharAt(userInput.length() - 1);
                }

                default -> {

                    if (userInput.length() > 8 || !String.copyValueOf(new char[]{keyEvent.getKeyChar()}).matches("[a-z0-9]"))
                        return;

                    userInput.append(keyEvent.getKeyChar());
                }
            }
        }
    }
}
