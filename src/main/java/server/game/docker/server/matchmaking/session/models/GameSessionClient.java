package server.game.docker.server.matchmaking.session.models;

import java.net.InetAddress;

/**
 * Server side player client representation
 */
public class GameSessionClient {
    private InetAddress iPAddress;
    private Integer port;
    private Long clientID;
    public InetAddress getiPAddress() {
        return iPAddress;
    }
    public void setiPAddress(InetAddress iPAddress) {
        this.iPAddress = iPAddress;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public Long getClientID() {
        return clientID;
    }
    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }
    public GameSessionClient(InetAddress iPAddress, Integer port, Long clientID) {
        this.iPAddress = iPAddress;
        this.port = port;
        this.clientID = clientID;
    }
}
