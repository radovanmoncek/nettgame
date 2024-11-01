package server.game.docker;

public final class GameServerLauncher {
    public static void main(String[] args) throws Exception {
        new GameServer(args).run();
    }
}
