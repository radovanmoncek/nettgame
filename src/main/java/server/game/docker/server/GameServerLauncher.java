package server.game.docker.server;

public class GameServerLauncher {
    public static void main(String[] args) throws Exception {
        new GameServer(args).run();
    }
}
