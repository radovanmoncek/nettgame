package server.game.docker.server.session.net.handlers.examples.game.models;

import java.net.InetAddress;

/**
 * Server side player client representation
 */
public class GameSessionClient {
    private final InetAddress clientIPAddress;
    private final Integer port;
    private final Long clientID;
    public InetAddress getClientIPAddress() {
        return clientIPAddress;
    }
    public Integer getPort() {
        return port;
    }
    public Long getClientID() {
        return clientID;
    }
    public GameSessionClient(InetAddress clientIPAddress, Integer port, Long clientID) {
        this.clientIPAddress = clientIPAddress;
        this.port = port;
        this.clientID = clientID;
    }

    public static class GameClientPlayer {
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
            gold = 10000;
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
}
