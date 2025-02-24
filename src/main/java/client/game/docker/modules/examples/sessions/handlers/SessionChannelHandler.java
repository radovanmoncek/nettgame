package client.game.docker.modules.examples.sessions.handlers;

import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SessionChannelHandler extends ChannelHandler<SessionResponseProtocolDataUnit, SessionRequestProtocolDataUnit> {
    private static final Logger logger = LogManager.getLogger(SessionChannelHandler.class);
    private final NetworkedGameGUI networkedGameGUI;
    /**
     * State (state machine) design pattern
     */
    private ClientState clientState;
    private boolean sessionHost = false;
    private final Player sessionHostPlayer;

    public SessionChannelHandler() {

        sessionHostPlayer = new Player();

        networkedGameGUI = new NetworkedGameGUI();
    }

    @Override
    protected void serverChannelRead(final SessionResponseProtocolDataUnit protocolDataUnit) {

        switch (protocolDataUnit.sessionFlag()) {

            case START -> clientState = new ClientState() {

                {

                    sessionHost = true;
                    networkedGameGUI.addObject(new Player());
                    networkedGameGUI.addObject(new Player());
                }

                @Override
                public void render(final Graphics2D graphics, final List<Player> objects) {

                    graphics.setColor(Color.WHITE);
                    graphics.drawString(protocolDataUnit.sessionUUID(), networkedGameGUI.getWidth() / 2, 20);
                    graphics.translate(objects.getFirst().x, objects.getFirst().y);
                    graphics.drawString(protocolDataUnit.nickname1(), 0, 0);
                    graphics.rotate(Math.toRadians(objects.getFirst().rotationAngle), 10, 10);
                    graphics.fillRect(0, 0, 20, 20);
                }

                public void onKeyPress(final KeyEvent keyEvent) {

                    sendPlayerInput(keyEvent, sessionHostPlayer.x, sessionHostPlayer.y);
                }
            };

            case JOIN -> clientState = new JoinSessionSelectedClientState() {

                @Override
                public void render(final Graphics2D graphics, final List<Player> objects) {

                    final var player2 = objects.getLast();

                    graphics.setColor(Color.WHITE);
                    graphics.drawString(protocolDataUnit.sessionUUID(), networkedGameGUI.getWidth() / 2, 20);
                    graphics.translate(objects.getFirst().x, objects.getFirst().y);
                    graphics.drawString(protocolDataUnit.nickname1(), 0, 4);
                    graphics.rotate(Math.toRadians(objects.getFirst().rotationAngle), 10, 10);
                    graphics.fillRect(0, 0, 20, 20);
                    graphics.rotate(-Math.toRadians(objects.getFirst().rotationAngle), 10, 10);
                    graphics.translate(-objects.getFirst().x, objects.getFirst().y);
                    graphics.translate(player2.x, player2.y);
                    graphics.drawString(protocolDataUnit.nickname2(), 0, 4);
                    graphics.rotate(Math.toRadians(player2.rotationAngle), 10, 10);
                    graphics.rotate(Math.toRadians(player2.rotationAngle), 10, 10);
                    graphics.fillRect(0, 0, 20, 20);
                }

                public void onKeyPress(final KeyEvent keyEvent) {

                    sendPlayerInput(keyEvent, sessionHostPlayer.x, sessionHostPlayer.y);
                }
            };

            case STATE -> { //todo: proper non-naive implementation (client-side prediction)

                final var player1 = networkedGameGUI.getObject(0);
                final var player2 = networkedGameGUI.getObject(1);

                sessionHostPlayer.x = sessionHost ? player1.x : player2.x;
                sessionHostPlayer.y = sessionHost ? player1.y : player2.y;
                sessionHostPlayer.rotationAngle = sessionHost ? player1.rotationAngle : player2.rotationAngle;

                player1.x = protocolDataUnit.x1();
                player1.y = protocolDataUnit.y1();
                player1.rotationAngle = protocolDataUnit.rotationAngle1();
                player2.x = protocolDataUnit.x2();
                player2.y = protocolDataUnit.y2();
                player2.rotationAngle = protocolDataUnit.rotationAngle2();
            }
        }

        logger.info("Session response received from the server {}", protocolDataUnit);
    }

    private void sendPlayerInput(final KeyEvent keyEvent, int x, int y) {

        switch (keyEvent.getKeyCode()) {

            case KeyEvent.VK_W -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x,
                    y - 8,
                    0
            ));

            case KeyEvent.VK_A -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x - 8,
                    y,
                    270
            ));

            case KeyEvent.VK_S -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x,
                    y + 8,
                    180
            ));

            case KeyEvent.VK_D -> unicastToServerChannel(SessionRequestProtocolDataUnit.newSTATE(
                    x + 8,
                    y,
                    90
            ));
        }
    }

    private interface ClientState {

        void render(final Graphics2D graphics, final List<Player> objects);

        void onKeyPress(final KeyEvent keyEvent);
    }

    private class MainMenuClientState implements ClientState {
        private boolean newSessionSelected = true, joinSessionSelected = false;

        @Override
        public void render(final Graphics2D graphics, final List<Player> objects) {

            if(networkedGameGUI == null) {

                return;
            }

            graphics.setColor(Color.WHITE);

            if (newSessionSelected)
                graphics.setColor(Color.YELLOW);

            final var newSessionText = "New Session";

            graphics
                    .drawString(
                            newSessionText,
                            networkedGameGUI.getWidth() / 2 - graphics.getFontMetrics().stringWidth(newSessionText) / 2,
                            networkedGameGUI.getHeight() / 2 - graphics.getFontMetrics().getHeight() / 2
                    );

            graphics.setColor(Color.WHITE);

            if (joinSessionSelected)
                graphics.setColor(Color.YELLOW);

            final var joinSessionText = "Join Session";

            graphics.drawString(
                    joinSessionText,
                    networkedGameGUI.getWidth() / 2 - graphics.getFontMetrics().stringWidth(joinSessionText) / 2,
                    networkedGameGUI.getHeight() / 2 + graphics.getFontMetrics().getHeight() / 2
            );
        }

        @Override
        public void onKeyPress(final KeyEvent keyEvent) {

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
        public void render(final Graphics2D graphics, List<Player> objects) {

            graphics.setColor(Color.BLUE);

            graphics.setFont(new Font("Arial", Font.BOLD, 12));

            graphics.drawRect(
                    networkedGameGUI.getWidth() / 2 - networkedGameGUI.getWidth() / 8,
                    networkedGameGUI.getHeight() / 2 - networkedGameGUI.getHeight() / 16,
                    networkedGameGUI.getWidth() / 4,
                    networkedGameGUI.getHeight() / 8
            );

            if (userInput.isEmpty()) {

                graphics.drawString("Please enter your nickname", networkedGameGUI.getWidth() / 2, networkedGameGUI.getHeight() / 2);

                return;
            }

            graphics.drawString(userInput.toString(), networkedGameGUI.getWidth() / 2, networkedGameGUI.getHeight() / 2);
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
        public void render(final Graphics2D graphics, final List<Player> objects) {

            graphics.setColor(Color.BLUE);

            graphics.setFont(new Font("Arial", Font.PLAIN, 12));

            graphics.drawRect(
                    networkedGameGUI.getWidth() / 2 - networkedGameGUI.getWidth() / 8,
                    networkedGameGUI.getHeight() / 2 - networkedGameGUI.getHeight() / 16,
                    networkedGameGUI.getWidth() / 4,
                    networkedGameGUI.getHeight() / 8
            );

            if (userInput.isEmpty()) {

                final var text = nickname == null ? "Please enter your nickname" : "Please enter session code";

                graphics
                        .drawString(
                                text,
                                networkedGameGUI.getWidth() / 2 - graphics.getFontMetrics().stringWidth(text) / 2,
                                networkedGameGUI.getHeight() / 2 - graphics.getFontMetrics().getHeight() / 2
                        );

                return;
            }

            graphics.drawString(userInput.toString(), networkedGameGUI.getWidth() / 2, networkedGameGUI.getHeight() / 2);
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

    private class NetworkedGameGUI extends JPanel {
        private final List<Player> objects;
        private boolean running;

        public NetworkedGameGUI() {

            clientState = new MainMenuClientState();

            objects = new ArrayList<>();

            running = true;

            final var frame = new JFrame("Example Networked Game Client");

            frame.setPreferredSize(new Dimension(800, 600));
            frame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(final WindowEvent e) {

                    running = false;
                }
            });
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().x - 400, GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().y - 300);
            frame.add(this);
            frame.setVisible(true);
            frame.pack();

            setCursor(getToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));

            requestFocusInWindow();

            final var runnable = (Runnable) () -> {

                while (running) {

                    try {

                        repaint();

                        TimeUnit.MILLISECONDS.sleep(30); // todo: proper fps system (timeDeltaTime) !!!!
                    } catch (InterruptedException interruptedException) {

                        logger.error(interruptedException.getMessage(), interruptedException);
                    }
                }

                try {

                    disconnectFromInstanceContainer();
                } catch (InterruptedException exception) {

                    logger.error(exception.getMessage(), exception);
                }
            };

            final var networkedGameGUIRenderingLoop = Executors
                    .defaultThreadFactory()
                    .newThread(runnable);

            networkedGameGUIRenderingLoop.setName("Networked Game GUI Rendering Loop");
            networkedGameGUIRenderingLoop.start();

            addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(final KeyEvent keyEvent) {

                    clientState.onKeyPress(keyEvent);
                }
            });
        }

        @Override
        public void paint(final Graphics graphics) {

            clear(graphics);

            if(clientState == null)
                return;

            clientState.render((Graphics2D) graphics, objects);
        }

        private void clear(final Graphics graphics) {

            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, getWidth(), getHeight());
        }

        public Player getObject(final int index) {

            return objects.get(index);
        }

        public void addObject(final Player player) {

            objects.add(player);
        }
    }

    private static class Player {
        int x = 1000;
        int y = 1000;
        int rotationAngle;
    }
}
