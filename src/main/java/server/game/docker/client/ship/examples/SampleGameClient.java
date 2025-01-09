package server.game.docker.client.ship.examples;

import server.game.docker.client.modules.lobby.facades.LobbyChannelFacade;
import server.game.docker.client.modules.messages.facades.ChatMessageChannelFacade;
import server.game.docker.client.modules.player.facades.PlayerChannelFacade;
import server.game.docker.client.modules.sessions.facades.SessionChannelFacade;
import server.game.docker.client.modules.state.facades.StateChannelFacade;
import server.game.docker.client.ship.bootstrap.GameClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SampleGameClient extends JPanel {
    private int xOffset = 0;
    private boolean running = true;
    private GameClient gameClient;
    private String nickname;
    private Long lobbyLeaderId;
    private Collection<String> lobbyMembers;
    private boolean sessionMember;
    private ArrayList<StateChannelFacade.GameEntity> gameEntities = new ArrayList<>();
    private ArrayList<ArrayList<String>> chat = new ArrayList<>();

    public SampleGameClient() throws Exception {
        final var frame = new JFrame("Sample Docker Game Client");
        frame.setSize(new Dimension(800, 600));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().x + xOffset, GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().y - 300);
        frame.add(this);
        frame.setVisible(true);

        gameClient = GameClient
                .newInstance()
                .withPlayerClientFacade(new PlayerChannelFacade() {
                    @Override
                    public void receiveNewNickname(String newNickname) {
                        nickname = newNickname;
                    }
                })
                .withLobbyClientFacade(new LobbyChannelFacade() {
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
                .withSessionClientFacade(new SessionChannelFacade() {
                    @Override
                    public void receiveStartSessionResponse() {
                        sessionMember = true;
                    }

                    @Override
                    public void receiveStopSessionResponse() {
                        sessionMember = false;
                    }
                })
                .withStateClientFacade(new StateChannelFacade() {
                    @Override
                    public void receiveState(List<GameEntity> gameEntities) {
                        SampleGameClient.this.gameEntities = new ArrayList<>(gameEntities);
                    }
                })
                .withChatMessageClientFacade(new ChatMessageChannelFacade() {
                    @Override
                    public void receivePlayerLobbyChatMessage(String playerNickname, String message) {
                        chat.add(new ArrayList<>(List.of(playerNickname, message)));
                    }
                });

        gameClient.run(0, 10);

        new Thread(() -> {
            while (running) {
                repaint();
                try {
                    TimeUnit.MILLISECONDS.sleep(30);
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
    public void paint(Graphics g) {
        clear(g);

        g.setColor(Color.WHITE); //todo: temporary colour !!!!

        if(lobbyMembers == null || gameEntities == null || lobbyMembers.size() < 2 || gameEntities.size() < 2)
            return;

        for (int i = 0; i < 2; i++) {
            final var entity = gameEntities.get(i);
            g.fillRect(entity.x(), entity.y(), 10, 10);
            g.drawString(lobbyMembers.stream().toList().get(i), entity.x(), entity.y() - 15);
        }
    }

    private void clear(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void moveUp(){
        gameClient.getStateClientFacade().requestState(gameEntities.get(0).x(), gameEntities.get(0).y() + 4);
    }

    public void moveDown(){
        gameClient.getStateClientFacade().requestState(gameEntities.get(0).x(), gameEntities.get(0).y() - 4);
    }

    public void moveLeft(){
        gameClient.getStateClientFacade().requestState(gameEntities.get(0).x() - 4, gameEntities.get(0).y());
    }

    public void moveRight(){
        gameClient.getStateClientFacade().requestState(gameEntities.get(0).x() + 4, gameEntities.get(0).y());
    }

    public void requestNewNickname(String nickname) {
        gameClient.getUsernameClientFacade().requestNickname(nickname);
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

    public ArrayList<StateChannelFacade.GameEntity> getGameEntities() {
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
}
