package server.game.docker;

import io.netty.channel.ChannelId;
import server.game.docker.client.modules.state.pdus.StateRequestPDU;
import server.game.docker.modules.chat.facades.ChatMessageServerFacade;
import server.game.docker.modules.session.facades.SessionServerFacade;
import server.game.docker.modules.state.facades.StateServerFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.Map;

public final class GameServerLauncher {
    public static void main(String[] args) throws Exception {
        GameServer
                .newInstance()
                .withSessionServerFacadeFactory(() -> new SessionServerFacade(){
                    private Long tickCounter = 0L;

                    @Override
                    public void receiveSessionTick(ChannelId playerId, Map<ChannelId, String> playerLobby, PDU protocolDataUnit, StateServerFacade stateServerFacade) {
                        System.out.printf("playerNickname: %s, playerLobby: %s, PDU: %s, ticks: %d\n", playerId, playerLobby, protocolDataUnit, ++tickCounter); //todo: log4j
                        if(protocolDataUnit instanceof StateRequestPDU stateRequestPDU) {
                            System.out.printf("Player:%s x:%d y:%d\n", playerId, stateRequestPDU.x(), stateRequestPDU.y());//todo: log4j
                            stateServerFacade.respondToStateRequest(playerLobby.get(playerId), stateRequestPDU.x(), stateRequestPDU.y(), playerLobby.keySet().toArray(ChannelId[]::new));
                        }
                    }
                })
                .withStateServerFacade(new StateServerFacade())
                .withChatMessageServerFacade(new ChatMessageServerFacade())
                .run();
    }
}
