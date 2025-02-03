package server.game.docker.ship.examples;

import io.netty.channel.ChannelId;
import client.modules.state.models.StateRequestPDU;
import server.game.docker.modules.session.facades.SessionChannelGroupFacade;
import server.game.docker.modules.state.facades.StateChannelGroupFacade;
import server.game.docker.ship.bootstrap.InstanceContainer;

import java.util.LinkedList;
import java.util.Objects;

public final class SampleInstanceContainer {
    public static void main(String[] args) throws Exception {
            InstanceContainer
                    .newInstance()
                    .withSessionServerFacadeFactory(() -> new SessionChannelGroupFacade() {
                        private static final Integer X_BOUND = 800, Y_BOUND = 600;
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
                                x1 = stateRequestPDU.x() < X_BOUND ? stateRequestPDU.x() : X_BOUND;
                                y1 = stateRequestPDU.y() < Y_BOUND ? stateRequestPDU.y() : Y_BOUND;
                            }
                            else if(playerId == player2){
                                x2 = stateRequestPDU.x() < X_BOUND ? stateRequestPDU.x() : X_BOUND;
                                y2 = stateRequestPDU.y() < Y_BOUND ? stateRequestPDU.y() : Y_BOUND;
                            }

                            System.out.printf("PlayerChannelId: %s, playerLobby: %s, PDU: %s, ticks: %d\n", playerId, playerLobby, stateRequestPDU, ++tickCounter); //todo: log4j
                            System.out.printf("Player:%s x:%d y:%d\n", playerId, stateRequestPDU.x(), stateRequestPDU.y());//todo: log4j
                            stateChannelGroupFacade.respondToStateRequest(x1, y1, x2, y2, playerLobby.toArray(ChannelId[]::new));
                        }
                    })
                    .run(4);
    }
}
