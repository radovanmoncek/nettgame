package server.game.docker.net.dto;

public class LeaveLobbyRes {
    private Boolean leader;

    public Boolean isLeader() {
        return leader;
    }

    public void setLeader(Boolean leader) {
        this.leader = leader;
    }
}
