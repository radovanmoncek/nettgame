package server.game.docker.net.dto;

public class JoinLobbyReq {
    private Long lobbyID;

    public Long getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(Long lobbyID) {
        this.lobbyID = lobbyID;
    }
}
