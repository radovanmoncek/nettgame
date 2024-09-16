package server.game.docker.server.matchmaking.session;

public class GameSessionLauncher {
    public static void main(String[] args) {
        System.out.println("Launching game session...");
        new GameSession(args);
    }
}
