package client.game.docker.ship.bootstrap.examples;

import client.game.docker.modules.examples.chats.handlers.ChatChannelHandler;
import client.game.docker.modules.examples.lobbies.decoders.LobbyResponseDecoder;
import client.game.docker.modules.examples.lobbies.encoders.LobbyRequestEncoder;
import client.game.docker.modules.examples.lobbies.handlers.LobbyChannelHandler;
import client.game.docker.modules.examples.sessions.decoders.SessionResponseDecoder;
import client.game.docker.modules.examples.sessions.encoders.SessionRequestEncoder;
import client.game.docker.modules.examples.sessions.handlers.SessionChannelHandler;
import client.game.docker.ship.bootstrap.GameClient;
import container.game.docker.modules.examples.chat.decoders.ChatMessageDecoder;
import container.game.docker.modules.examples.chat.encoders.ChatMessageEncoder;

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

        final var frame = new JFrame("Sample Docker Game Client");

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
                .run();

        new Thread(
                () -> {

            while (running) {

                try {

                    repaint();

                    TimeUnit.MILLISECONDS.sleep(30); // todo: proper fps system (timeDeltaTime) !!!!
                } catch (InterruptedException e) {

                    e.printStackTrace(); // todo: log4j
                }
            }

            try {

                gameClient.shutdownGracefullyAfterNSeconds(0);
            } catch (InterruptedException e) {

                e.printStackTrace(); //todo: log4j
            }
        },
                "Client Main Event Loop"
        )
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
