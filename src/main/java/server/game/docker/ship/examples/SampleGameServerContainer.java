package server.game.docker.ship.examples;

import io.netty.channel.ChannelId;
import server.game.docker.client.modules.state.pdus.StateRequestPDU;
import server.game.docker.modules.session.facades.SessionChannelGroupFacade;
import server.game.docker.modules.state.facades.StateChannelGroupFacade;
import server.game.docker.ship.bootstrap.GameServerContainer;

import java.util.LinkedList;
import java.util.Objects;

public final class SampleGameServerContainer {
    public static void main(String[] args) throws Exception {
            GameServerContainer
                    .newInstance()
                    .withSessionServerFacadeFactory(() -> new SessionChannelGroupFacade() {
                        private Long tickCounter = 0L;
                        private ChannelId player1, player2;
                        private Integer x1 = 0, y1 = 0, x2 = 0, y2 = 0;

                        @Override
                        public void receiveSessionTick(ChannelId playerId, LinkedList<ChannelId> playerLobby, StateRequestPDU stateRequestPDU, StateChannelGroupFacade stateChannelGroupFacade) {
                            if(Objects.isNull(stateRequestPDU))
                                return;

                            if(player1 == null || player2 == null) {
                                if(player1 == null)
                                    player1 = playerId;
                                else
                                    player2 = playerId;
                            }

                            if(playerId == player1){
                                x1 = stateRequestPDU.x();
                                y1 = stateRequestPDU.y();
                            }
                            else if(playerId == player2){
                                x2 = stateRequestPDU.x();
                                y2 = stateRequestPDU.y();
                            }

                            System.out.printf("PlayerChannelId: %s, playerLobby: %s, PDU: %s, ticks: %d\n", playerId, playerLobby, stateRequestPDU, ++tickCounter); //todo: log4j
                            System.out.printf("Player:%s x:%d y:%d\n", playerId, stateRequestPDU.x(), stateRequestPDU.y());//todo: log4j
                            stateChannelGroupFacade.respondToStateRequest(x1, y1, x2, y2, playerLobby.toArray(ChannelId[]::new));
                        }
                    })
                    .run(4);
    }
}
