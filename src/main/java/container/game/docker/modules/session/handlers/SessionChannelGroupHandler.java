package container.game.docker.modules.session.handlers;

import container.game.docker.modules.session.models.SessionProtocolDataUnit;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import container.game.docker.modules.lobby.facades.LobbyChannelGroupHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class SessionChannelGroupHandler extends ChannelGroupHandler<ProtocolDataUnit> {
    private final ThreadGroup managedSessions;
    private final LobbyChannelGroupHandler lobbyChannelGroupHandler;
    private static final ConcurrentHashMap<ChannelId, Queue<SessionMessage>> sessionMembers = new ConcurrentHashMap<>();

    public SessionChannelGroupHandler(
            final LobbyChannelGroupHandler lobbyChannelGroupHandler,
            final ThreadGroup managedSessions
    ) {
        this.managedSessions = managedSessions;
        this.lobbyChannelGroupHandler = lobbyChannelGroupHandler;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final ProtocolDataUnit protocolDataUnit) {
        if (protocolDataUnit instanceof SessionProtocolDataUnit sessionProtocolDataUnit) {
            switch (sessionProtocolDataUnit.sessionFlag()) {
                case 0 -> {
                    System.out.printf("A player has requested session start %s \n", sessionProtocolDataUnit); //todo: log4j
                    startSession(channelHandlerContext.channel());
                }
                case 1 -> {
                    System.out.printf("A player has requested session end %s \n", sessionProtocolDataUnit); //todo: log4j
                    sessionMembers.get(channelHandlerContext.channel().id()).offer(
                            new SessionMessage(sessionProtocolDataUnit, channelHandlerContext)
                    );
                }
            }

            return;
        }

        sessionMembers
                .get(channelHandlerContext.channel().id())
                .offer(new SessionMessage(protocolDataUnit, channelHandlerContext));
    }

    private void startSession(final Channel clientChannel) {
        sessionMembers.putIfAbsent(clientChannel.id(), new ConcurrentLinkedQueue<>());

        lobbyChannelGroupHandler.findPlayerLobby(clientChannel.id()).ifPresent(playerLobby ->
                playerLobby.stream().filter(playerId -> !clientChannel.id().equals(playerId) && sessionMembers.containsKey(playerId)).findAny().ifPresent(playerId -> {
                    final var messageQueue = new LinkedList<SessionMessage>();

                    new Thread(managedSessions, () -> {
                        boolean ended = false;

                        sessionMembers.put(clientChannel.id(), messageQueue);
                        sessionMembers.put(playerId, messageQueue);

                        try {
                            receiveSessionStart(playerLobby.toArray(ChannelId[]::new));

                            while (!ended) {
                                final var sessionMessage = messageQueue.poll();

                                if(Objects.isNull(sessionMessage))
                                    return;

                                if (sessionMessage.protocolDataUnit instanceof SessionProtocolDataUnit sessionPDU) {
                                    if (sessionPDU.sessionFlag() == 1) {
                                        receiveSessionEnd(playerLobby.toArray(ChannelId[]::new));
                                        ended = true;
                                    }
                                }

                                receiveSessionTick(sessionMessage);

                                try {
                                    TimeUnit.MILLISECONDS.sleep(33);
                                } catch (InterruptedException e) {
                                    e.printStackTrace(); //todo: log4j
                                }
                            }

                            System.out.printf("Session with messageQueue: %s has ended\n", messageQueue); //todo: log4j

                            managedSessions.list();

                            sessionMembers.remove(playerLobby.get(0));
                            sessionMembers.remove(playerLobby.get(1));
                        } catch (Exception e) {
                            e.printStackTrace(); //todo: log4j
                        }
                    }).start();

                    managedSessions.list();
                })
        );
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
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

    protected void receiveSessionStart(final ChannelId... players) {
        final var sessionPDU = new SessionProtocolDataUnit((byte) SessionProtocolDataUnit.SessionFlag.START.ordinal());
        multicastPDUToClientChannelIds(sessionPDU, players);
    }

    protected void receiveSessionEnd(final ChannelId... players) {
        final var sessionPDU = new SessionProtocolDataUnit((byte) SessionProtocolDataUnit.SessionFlag.STOP.ordinal());
        multicastPDUToClientChannelIds(sessionPDU, players);
    }

    protected void receiveSessionTick(final SessionMessage sessionMessage) {
        sessionMessage.channelHandlerContext.fireChannelRead(sessionMessage.protocolDataUnit);
    }
}
