package server.game.docker.server;

import java.net.InetAddress;

/**
 * Server side player client representation
 */
public class GameClientPlayer {
    private InetAddress iPAddress;
    private Integer port;
    private String username;
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
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public GameClientPlayer(InetAddress iPAddress, Integer port, String username) {
        this.iPAddress = iPAddress;
        this.port = port;
        this.username = username;
    }
}
