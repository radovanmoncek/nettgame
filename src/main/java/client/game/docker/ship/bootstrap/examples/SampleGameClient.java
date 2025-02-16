package client.game.docker.ship.bootstrap.examples;

import client.game.docker.modules.examples.chats.handlers.ChatChannelHandler;
import client.game.docker.modules.examples.lobbies.codecs.LobbyResponseDecoder;
import client.game.docker.modules.examples.lobbies.codecs.LobbyRequestEncoder;
import client.game.docker.modules.examples.lobbies.handlers.LobbyChannelHandler;
import client.game.docker.modules.examples.sessions.codecs.SessionResponseDecoder;
import client.game.docker.modules.examples.sessions.codecs.SessionRequestEncoder;
import client.game.docker.modules.examples.sessions.handlers.SessionChannelHandler;
import client.game.docker.ship.bootstrap.GameClient;
import container.game.docker.modules.examples.chat.codecs.ChatMessageDecoder;
import container.game.docker.modules.examples.chat.codecs.ChatMessageEncoder;
import container.game.docker.modules.examples.chat.models.ChatMessageProtocolDataUnit;
import container.game.docker.modules.examples.lobby.models.LobbyRequestProtocolDataUnit;
import container.game.docker.modules.examples.lobby.models.LobbyResponseProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SampleGameClient extends JPanel {
    private final GameClient gameClient;
    private boolean running;
    private Consumer<Graphics> onPaint;

    public SampleGameClient() throws Exception {

        running = true;

        onPaint = graphics -> {};

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

        (gameClient = GameClient.newInstance())
                .withDecoder(SessionResponseDecoder::new)
                .withDecoder(LobbyResponseDecoder::new)
                .withDecoder(ChatMessageDecoder::new)
                .withChannelHandler(() -> new LobbyChannelHandler(SampleGameClient.this))
                .withChannelHandler(() -> new SessionChannelHandler(SampleGameClient.this))
                .withChannelHandler(() -> new ChatChannelHandler(SampleGameClient.this))
                .withEncoder(SessionRequestEncoder::new)
                .withEncoder(LobbyRequestEncoder::new)
                .withEncoder(ChatMessageEncoder::new)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 1, ChatMessageProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 2, LobbyRequestProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 3, LobbyResponseProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 4, SessionRequestProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 5, SessionResponseProtocolDataUnit.class)
                .run();

        final var runnable = (Runnable) () -> {

            while (running) {

                try {

                    repaint();

                    TimeUnit.MILLISECONDS.sleep(30); // todo: proper fps system (timeDeltaTime) !!!!
                } catch (InterruptedException e) {

                    e.printStackTrace(); // todo: log4j
                }
            }

            gameClient.shutdownGracefullyAfterNSeconds(0);
        };

        new Thread(runnable, "Client Game Rendering Loop")
                .start();
    }

    @Override
    public void paint(final Graphics graphics) {

        clear(graphics);

        onPaint.accept(graphics);
    }

    private void clear(final Graphics graphics) {

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    public static void main(String[] args) throws Exception {

        new SampleGameClient();
    }

    public void setOnPaint(final Consumer<Graphics> onPaint) {
        this.onPaint = onPaint;
    }
}
