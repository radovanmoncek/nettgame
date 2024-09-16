package server.game.docker.server.matchmaking.session.chat;

import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDUTypes;
import server.game.docker.server.matchmaking.session.GameSession;
import server.game.docker.server.matchmaking.session.examples.game.models.GameClientPlayer;

import java.util.LinkedList;
import java.util.List;

public class SessionChat {
    private final GameSession gameSession;
    private final List<String> messageHistoryBuffer; //todo: will get replaced by persistence module (Redis)

    public SessionChat(GameSession gameSession) {
        this.gameSession = gameSession;
        gameSession.getActionMapper()
                .withActionMapping(MyPDUTypes.CHATMESSAGE.getID(), p -> {
                    appendMessageToHistoryBuffer(p.decode().get(1));
                    GameClientPlayer gameClientPlayer = gameSession.connectedClients.stream().filter(c -> c.getClientID().equals(Long.parseLong(p.decode().get(0)))).map(GameClientPlayer::new).findAny().orElse(null);
                    if(gameClientPlayer == null)
                        return;
                    gameSession.connectedClients.stream().filter(c -> !c.getClientID().equals(Long.valueOf(p.decode().get(0)))).forEach(c -> gameSession.sendUnicast(new MyPDU(MyPDUTypes.CHATMESSAGE.getID(), p.decode().get(1), gameClientPlayer.getUsername()).withIPAndPort(c.getClientIPAddress(), c.getPort())));
                });
        messageHistoryBuffer = new LinkedList<>();
    }

    private synchronized void appendMessageToHistoryBuffer(String message) {
        messageHistoryBuffer.add(message);
    }
}
