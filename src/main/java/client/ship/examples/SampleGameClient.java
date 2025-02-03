package client.ship.examples;

import client.modules.lobby.handlers.LobbyClientHandler;
import client.modules.messages.handlers.ChatMessageClientHandler;
import client.modules.player.handlers.PlayerClientHandler;
import client.modules.sessions.handlers.SessionClientHandler;
import client.modules.state.handlers.StateClientHandler;
import client.ship.bootstrap.GameClient;
import io.netty.channel.ChannelHandlerContext;
import server.game.docker.modules.player.pdus.NicknamePDU;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SampleGameClient extends JPanel {
    private ChatMessageClientHandler chatMessageClientHandler;
    private SessionClientHandler sessionClientHandler;
    private int xOffset = 0;
    private boolean running = true;
    private GameClient gameClient;
    private String nickname;
    private Long lobbyLeaderId;
    private Collection<String> lobbyMembers;
    private boolean sessionMember;
    private ArrayList<StateClientHandler.GameEntity> gameEntities = new ArrayList<>();
    private ArrayList<ArrayList<String>> chat = new ArrayList<>();
    private int currentMouseX = -1, currentMouseY = -1;
    private String nicknameTemporaryBuffer = "", lobbyCodeTemporaryBuffer = "";
    private StateClientHandler stateClientHandler;
    private PlayerClientHandler playerClientHandler;

    public SampleGameClient() throws Exception {
        final var frame = new JFrame("Sample Docker Game Client");
        frame.setPreferredSize(new Dimension(800, 600));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                running = false;
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().x + xOffset, GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().y - 300);
        frame.add(this);
        frame.setVisible(true);
        frame.pack();

        setCursor(getToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if(!lobbyCodeTemporaryBuffer.isEmpty())
                        lobbyCodeTemporaryBuffer = lobbyCodeTemporaryBuffer.substring(0, lobbyCodeTemporaryBuffer.length() - 1);

                    if(!nicknameTemporaryBuffer.isEmpty())
                        nicknameTemporaryBuffer = nicknameTemporaryBuffer.substring(0, nicknameTemporaryBuffer.length() - 1);

                    return;
                }

                if(nicknameTemporaryBuffer.length() > 8 || lobbyCodeTemporaryBuffer.length() > 10 || !String.copyValueOf(new char[] {e.getKeyChar()}).matches("[a-z]"))
                    return;

                if(nickname == null)
                    nicknameTemporaryBuffer += e.getKeyChar();

                else lobbyCodeTemporaryBuffer += e.getKeyChar();
            }
        });

        requestFocusInWindow();

        gameClient = GameClient
                .newInstance()
                .withPlayerClientHandlerSupplier(() -> playerClientHandler = new PlayerClientHandler() {

                    @Override
                    public void channelRead0(ChannelHandlerContext channelHandlerContext, NicknamePDU nicknamePDU) {
                        nickname = nicknamePDU.nickname();
                    }
                })
                .withLobbyClientHandlerSupplier(() -> new LobbyClientHandler() {
                    @Override
                    public void receiveLobbyLeft(Long leaderId, Collection<String> members) {
                        lobbyLeaderId = leaderId;
                        lobbyMembers = members;
                    }

                    @Override
                    public void receiveLobbyJoined(Long leaderId, Collection<String> members) {
                        lobbyLeaderId = leaderId;
                        lobbyMembers = members;
                    }

                    @Override
                    public void receiveLobbyCreated(Long leaderId, Collection<String> members) {
                        lobbyLeaderId = leaderId;
                        lobbyMembers = members;
                    }

                    @Override
                    public void receiveLobbyMemberJoined(Long aLong, Collection<String> members) {
                        lobbyLeaderId = aLong;
                        lobbyMembers = members;
                    }

                    @Override
                    public void receiveLobbyMemberLeft(Long leaderId, Collection<String> members) {
                        lobbyLeaderId = leaderId;
                        lobbyMembers = members;
                    }
                })
                .withSessionClientHandlerSupplier(() -> sessionClientHandler = new SessionClientHandler() {
                    @Override
                    public void receiveStartSessionResponse() {
                        sessionMember = true;
                    }

                    @Override
                    public void receiveStopSessionResponse() {
                        sessionMember = false;
                    }
                })
                .withStateClientHandlerSupplier(() -> stateClientHandler = new StateClientHandler() {
                    @Override
                    public void receiveState(List<GameEntity> gameEntities) {
                        SampleGameClient.this.gameEntities = new ArrayList<>(gameEntities);
                    }
                })
                .withChatMessageClientHandlerSupplier(() -> chatMessageClientHandler = new ChatMessageClientHandler() {
                    @Override
                    public void receivePlayerLobbyChatMessage(String playerNickname, String message) {
                        chat.add(new ArrayList<>(List.of(playerNickname, message)));
                    }
                });

        gameClient.run(0, 10);

        new Thread(() -> {
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
        }).start();
    }

    @Override
    public void paint(Graphics graphics) {
        clear(graphics);

        drawCursor(graphics);

        if(nickname == null)
            drawEnterNicknameScreen(graphics);

        else if(lobbyLeaderId == null)
            drawEnterLobbyCodeScreen(graphics);

        if(lobbyMembers == null || gameEntities == null || lobbyMembers.size() < 2 || gameEntities.size() < 2)
            return;

        graphics.setColor(Color.WHITE); //todo: temporary colour !!!!

        for (int i = 0; i < 2; i++) {
            final var entity = gameEntities.get(i);
            graphics.fillRect(entity.x(), entity.y(), 10, 10);
            graphics.drawString(lobbyMembers.stream().toList().get(i), entity.x(), entity.y() - 15);
        }
    }

    private void clear(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawCursor(final Graphics graphics){
        graphics.setColor(Color.ORANGE);

        graphics.drawRect(currentMouseX, currentMouseY, 1, 1);
    }

    private void drawEnterNicknameScreen(final Graphics graphics) {
        graphics.setColor(Color.WHITE);

        graphics.drawString(nicknameTemporaryBuffer.isBlank() ? "Please enter your nickname" : nicknameTemporaryBuffer, getWidth() / 2 - 8 * 3, getHeight() / 2);
    }

    private void drawEnterLobbyCodeScreen(final Graphics graphics) {
        graphics.setColor(Color.WHITE);

        graphics.drawString(lobbyCodeTemporaryBuffer.isBlank() ? "Please enter lobby code" : lobbyCodeTemporaryBuffer, getWidth() / 2 - lobbyCodeTemporaryBuffer.length() * 3, getHeight() / 2);
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void moveUp(){
        stateClientHandler.requestState(gameEntities.get(0).x(), gameEntities.get(0).y() + 4);
    }

    public void moveDown(){
        stateClientHandler.requestState(gameEntities.get(0).x(), gameEntities.get(0).y() - 4);
    }

    public void moveLeft(){
        stateClientHandler.requestState(gameEntities.get(0).x() - 4, gameEntities.get(0).y());
    }

    public void moveRight(){
        stateClientHandler.requestState(gameEntities.get(0).x() + 4, gameEntities.get(0).y());
    }

    public void requestNewNickname(String nickname) {
        playerClientHandler.requestNickname(nickname);
    }

    public String getNickname() {
        return nickname;
    }

    public Long getLobbyLeaderId() {
        return lobbyLeaderId;
    }

    public boolean isSessionMember() {
        return sessionMember;
    }

    public ArrayList<StateClientHandler.GameEntity> getGameEntities() {
        return gameEntities;
    }

    public ArrayList<ArrayList<String>> getChat() {
        return chat;
    }

    public Collection<String> getLobbyMembers() {
        return lobbyMembers;
    }

    public GameClient getGameClient() {
        return gameClient;
    }

    public static void main(String[] args) throws Exception {
        new SampleGameClient();
    }
}
