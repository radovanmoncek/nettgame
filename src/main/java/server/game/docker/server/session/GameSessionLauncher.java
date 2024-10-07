package server.game.docker.server.session;

public class GameSessionLauncher {
    public static void main(String[] args) {
        System.out.println("Launching game session...");
        new DockerGameSession(args);
    }
}
