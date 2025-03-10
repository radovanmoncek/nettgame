package cz.radovanmoncek.client.modules.games.handlers;

import cz.radovanmoncek.server.modules.games.models.GameStateFlatBufferSerializable;
import cz.radovanmoncek.client.modules.games.models.GameStateRequestFlatBuffersSerializable;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStatus;
import cz.radovanmoncek.client.ship.parents.handlers.ServerChannelHandler;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import io.netty.channel.ChannelHandlerContext;
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

public class ExampleServerChannelHandler extends ServerChannelHandler<GameState> {
    private static final Logger logger = LogManager.getLogger(ExampleServerChannelHandler.class);
    private final GameRenderer gameRenderer;
    /**
     * State (state machine) design pattern
     */
    private ClientState clientState;
    private boolean sessionHost = false;
    private GameStateFlatBufferSerializable.Player sessionHostPlayer;

    public ExampleServerChannelHandler() {

        sessionHostPlayer = new GameStateFlatBufferSerializable.Player(1000, 1000, 0, "");
        gameRenderer = new GameRenderer();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final GameState gameState) {

        switch (gameState.game().status()) {

            case GameStatus.START_SESSION -> clientState = new ClientState() {

                {

                    sessionHost = true;
                    gameRenderer.addObject(new GameStateFlatBufferSerializable.Player(1000, 1000, 0, ""));
                    gameRenderer.addObject(new GameStateFlatBufferSerializable.Player(1000, 1000, 0, ""));
                }

                @Override
                public void render(final Graphics2D graphics, final List<GameStateFlatBufferSerializable.Player> objects) {

                    graphics.setColor(Color.WHITE);
                    graphics.drawString(gameState.game().gameCode(), gameRenderer.getWidth() / 2, 20);
                    graphics.translate(objects.getFirst().x(), objects.getFirst().y());
                    graphics.drawString(gameState.player1().name(), 0, 0);
                    graphics.rotate(Math.toRadians(objects.getFirst().rotationAngle()), 10, 10);
                    graphics.fillRect(0, 0, 20, 20);
                }

                public void onKeyPress(final KeyEvent keyEvent) {

                    sendPlayerInput(keyEvent, sessionHostPlayer.x(), sessionHostPlayer.y());
                }
            };

            case GameStatus.JOIN_SESSION -> clientState = new JoinSessionSelectedClientState() {

                @Override
                public void render(final Graphics2D graphics, final List<GameStateFlatBufferSerializable.Player> objects) {

                    final var player2 = objects.getLast();

                    graphics.setColor(Color.WHITE);
                    graphics.drawString(gameState.game().gameCode(), gameRenderer.getWidth() / 2, 20);
                    graphics.translate(objects.getFirst().x(), objects.getFirst().y());
                    graphics.drawString(gameState.player1().name(), 0, 4);
                    graphics.rotate(Math.toRadians(objects.getFirst().rotationAngle()), 10, 10);
                    graphics.fillRect(0, 0, 20, 20);
                    graphics.rotate(-Math.toRadians(objects.getFirst().rotationAngle()), 10, 10);
                    graphics.translate(-objects.getFirst().x(), objects.getFirst().y());
                    graphics.translate(player2.x(), player2.y());
                    graphics.drawString(gameState.player2().name(), 0, 4);
                    graphics.rotate(Math.toRadians(player2.rotationAngle()), 10, 10);
                    graphics.rotate(Math.toRadians(player2.rotationAngle()), 10, 10);
                    graphics.fillRect(0, 0, 20, 20);
                }

                public void onKeyPress(final KeyEvent keyEvent) {

                    sendPlayerInput(keyEvent, sessionHostPlayer.x(), sessionHostPlayer.y());
                }
            };

            case GameStatus.STATE_CHANGE -> { //todo: proper non-naive implementation (client-side prediction)

                final var player1 = gameRenderer.getObject(0);
                final var player2 = gameRenderer.getObject(1);

                sessionHostPlayer = new GameStateFlatBufferSerializable.Player(
                        sessionHost ? player1.x() : player2.x(),
                        sessionHost ? player1.y() : player2.y(),
                        sessionHost ? player1.rotationAngle() : player2.rotationAngle(),
                        sessionHost ? player1.name() : player2.name()
                );

                gameRenderer.clearObjects();

                gameRenderer.addObject(new GameStateFlatBufferSerializable.Player(
                        gameState.player1().x(),
                        gameState.player1().y(),
                        gameState.player1().rotationAngle(),
                        gameState.player1().name()
                ));

                if (gameState.player2() == null) {

                    gameRenderer.addObject(new GameStateFlatBufferSerializable.Player(0, 0, 0, ""));

                    return;
                }

                gameRenderer.addObject(new GameStateFlatBufferSerializable.Player(
                        gameState.player2().x(),
                        gameState.player2().y(),
                        gameState.player2().rotationAngle(),
                        gameState.player2().name()
                ));
            }
        }

        logger.info("Session response received from the server {}", gameState);
    }

    private void sendPlayerInput(final KeyEvent keyEvent, int x, int y) {

        int requestedX = x;
        int requestedY = y;
        int requestedRotationAngle = 0;

        switch (keyEvent.getKeyCode()) {

            case KeyEvent.VK_W -> requestedY = y - 8;

            case KeyEvent.VK_A -> {
                requestedX = x - 8;
                requestedRotationAngle = 270;
            }

            case KeyEvent.VK_S -> {
                requestedY = y + 8;
                requestedRotationAngle = 180;
            }

            case KeyEvent.VK_D -> {
                requestedX = x + 8;
                requestedRotationAngle = 90;
            }

            default -> {

                return;
            }
        }

        unicast(new GameStateRequestFlatBuffersSerializable(requestedX, requestedY, requestedRotationAngle, "", GameStatus.STATE_CHANGE, ""));
    }

    private interface ClientState {

        void render(final Graphics2D graphics, final List<GameStateFlatBufferSerializable.Player> objects);

        void onKeyPress(final KeyEvent keyEvent);
    }

    private class MainMenuClientState implements ClientState {
        private boolean newSessionSelected = true, joinSessionSelected = false;

        @Override
        public void render(final Graphics2D graphics, final List<GameStateFlatBufferSerializable.Player> objects) {

            if (gameRenderer == null) {

                return;
            }

            graphics.setColor(Color.WHITE);

            if (newSessionSelected)
                graphics.setColor(Color.YELLOW);

            final var newSessionText = "New Session";

            graphics
                    .drawString(
                            newSessionText,
                            gameRenderer.getWidth() / 2 - graphics.getFontMetrics().stringWidth(newSessionText) / 2,
                            gameRenderer.getHeight() / 2 - graphics.getFontMetrics().getHeight() / 2
                    );

            graphics.setColor(Color.WHITE);

            if (joinSessionSelected)
                graphics.setColor(Color.YELLOW);

            final var joinSessionText = "Join Session";

            graphics.drawString(
                    joinSessionText,
                    gameRenderer.getWidth() / 2 - graphics.getFontMetrics().stringWidth(joinSessionText) / 2,
                    gameRenderer.getHeight() / 2 + graphics.getFontMetrics().getHeight() / 2
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
        public void render(final Graphics2D graphics, List<GameStateFlatBufferSerializable.Player> objects) {

            graphics.setColor(Color.BLUE);

            graphics.setFont(new Font("Arial", Font.BOLD, 12));

            graphics.drawRect(
                    gameRenderer.getWidth() / 2 - gameRenderer.getWidth() / 8,
                    gameRenderer.getHeight() / 2 - gameRenderer.getHeight() / 16,
                    gameRenderer.getWidth() / 4,
                    gameRenderer.getHeight() / 8
            );

            if (userInput.isEmpty()) {

                graphics.drawString("Please enter your nickname", gameRenderer.getWidth() / 2, gameRenderer.getHeight() / 2);

                return;
            }

            graphics.drawString(userInput.toString(), gameRenderer.getWidth() / 2, gameRenderer.getHeight() / 2);
        }

        @Override
        public void onKeyPress(KeyEvent keyEvent) {

            switch (keyEvent.getKeyCode()) {

                case KeyEvent.VK_ENTER -> {

                    if (userInput.isEmpty())
                        return;

                    unicast(new GameStateRequestFlatBuffersSerializable(0, 0, 0, userInput.toString(), GameStatus.START_SESSION, ""));
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
        public void render(final Graphics2D graphics, final List<GameStateFlatBufferSerializable.Player> objects) {

            graphics.setColor(Color.BLUE);

            graphics.setFont(new Font("Arial", Font.PLAIN, 12));

            graphics.drawRect(
                    gameRenderer.getWidth() / 2 - gameRenderer.getWidth() / 8,
                    gameRenderer.getHeight() / 2 - gameRenderer.getHeight() / 16,
                    gameRenderer.getWidth() / 4,
                    gameRenderer.getHeight() / 8
            );

            if (userInput.isEmpty()) {

                final var text = nickname == null ? "Please enter your nickname" : "Please enter session code";

                graphics
                        .drawString(
                                text,
                                gameRenderer.getWidth() / 2 - graphics.getFontMetrics().stringWidth(text) / 2,
                                gameRenderer.getHeight() / 2 - graphics.getFontMetrics().getHeight() / 2
                        );

                return;
            }

            graphics.drawString(userInput.toString(), gameRenderer.getWidth() / 2, gameRenderer.getHeight() / 2);
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

                    unicast(new GameStateRequestFlatBuffersSerializable(0, 0, 0, nickname, GameStatus.JOIN_SESSION, userInput.toString()));
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

    private class GameRenderer extends JPanel {
        private final List<GameStateFlatBufferSerializable.Player> objects;
        private boolean running;

        public GameRenderer() {

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

                disconnect();
            };

            final var networkedGameGUIRenderingLoop = Executors
                    .defaultThreadFactory()
                    .newThread(runnable);

            networkedGameGUIRenderingLoop.setName("Game Rendering Loop");
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

            if (clientState == null)
                return;

            clientState.render((Graphics2D) graphics, objects);
        }

        private void clear(final Graphics graphics) {

            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, getWidth(), getHeight());
        }

        public GameStateFlatBufferSerializable.Player getObject(final int index) {

            return objects.get(index);
        }

        public void addObject(final GameStateFlatBufferSerializable.Player player) {

            objects.add(player);
        }

        public void clearObjects() {

            objects.clear();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        super.exceptionCaught(ctx, cause);
    }
}
