package server.game.docker.server.matchmaking.session.models;

import java.net.InetAddress;

/**
 * Server side player client representation
 */
public class GameSessionClient {
    private InetAddress clientIPAddress;
    private Integer port;
    private Long clientID;
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
}
