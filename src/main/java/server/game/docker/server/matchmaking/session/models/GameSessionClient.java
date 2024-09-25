package server.game.docker.server.matchmaking.session.models;

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
}
