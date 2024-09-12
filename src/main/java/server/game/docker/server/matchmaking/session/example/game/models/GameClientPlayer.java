package server.game.docker.server.matchmaking.session.example.game.models;

import server.game.docker.server.matchmaking.session.models.GameSessionClient;

public class GameClientPlayer {
    private final Long clientID;
    private String username;
    private Boolean winner;
    private Integer gold;
    private Byte [] maximumOwnableArea;
    public Boolean getWinner() {
        return winner;
    }
    public Byte [] getMaximumOwnableArea() {
        return maximumOwnableArea;
    }
    public void setMaximumOwnableArea(Byte [] maximumOwnableArea) {
        this.maximumOwnableArea = maximumOwnableArea;
    }
    public void setWinner(Boolean winner) {
        this.winner = winner;
    }
    public Integer getGold() {
        return gold;
    }
    public void setGold(Integer gold) {
        this.gold = gold;
    }
    public GameClientPlayer(final GameSessionClient gameClient){
        clientID = gameClient.getClientID();
        gold = 1000;
        setUsername("Player " + gameClient.getClientID().toString());
    }
    public Long getClientID() {
        return clientID;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
