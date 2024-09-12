package server.game.docker.server.matchmaking.session;

public class GameSessionLauncher {
    public static void main(String[] args) {
        new GameSession(args)/*.init(args)*/;
    }
}
