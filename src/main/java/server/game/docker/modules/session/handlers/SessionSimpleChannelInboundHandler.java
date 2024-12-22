package server.game.docker.modules.session.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.state.pdus.StateRequestPDU;
import server.game.docker.modules.lobby.facades.LobbyChannelGroupFacade;
import server.game.docker.modules.session.facades.SessionChannelGroupFacade;
import server.game.docker.modules.session.pdus.SessionPDU;
import server.game.docker.modules.state.facades.StateChannelGroupFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SessionSimpleChannelInboundHandler extends SimpleChannelInboundHandler<PDU> {
    private final Supplier<SessionChannelGroupFacade> sessionChannelGroupFacadeFactory;
    private final StateChannelGroupFacade stateChannelGroupFacade;
    private final ThreadGroup managedSessions;
    private final LobbyChannelGroupFacade lobbyServerFacade;
    private static final ConcurrentHashMap<ChannelId, Queue<SessionMessage>> sessionMembers = new ConcurrentHashMap<>();

    public SessionSimpleChannelInboundHandler(
            final Supplier<SessionChannelGroupFacade> sessionChannelGroupFacadeFactory,
            final StateChannelGroupFacade stateChannelGroupFacade,
            final LobbyChannelGroupFacade lobbyServerFacade,
            final ThreadGroup managedSessions
            ) {
        this.sessionChannelGroupFacadeFactory = sessionChannelGroupFacadeFactory;
        this.stateChannelGroupFacade = stateChannelGroupFacade;
        this.managedSessions = managedSessions;
        this.lobbyServerFacade = lobbyServerFacade;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final PDU protocolDataUnit) {
        if (protocolDataUnit instanceof SessionPDU sessionPDU) {
            switch (sessionPDU.sessionFlag()) {
                case 0 -> {
                    System.out.printf("A player has requested session start %s \n", protocolDataUnit);
                    startSession(channelHandlerContext.channel());
                }
                case 2 ->{
                    System.out.printf("A player has requested session end or has disconnected %s \n", protocolDataUnit);
                    final var sessionMessageQueue = sessionMembers.remove(channelHandlerContext.channel().id());
                    lobbyServerFacade
                            .findPlayerLobby(channelHandlerContext.channel().id())
                            .stream()
                            .flatMap(Collection::stream)
                            .filter(playerId -> !playerId.equals(channelHandlerContext.channel().id()) && !sessionMembers.containsKey(playerId))
                            .findAny()
                            .ifPresent(playerId -> sessionMessageQueue.offer(new SessionMessage(channelHandlerContext.channel().id(), sessionPDU)));
                }
                case 1 ->{
                    //TODO:
                }
            }

            return;
        }

        sessionMembers.get(channelHandlerContext.channel().id()).offer(new SessionMessage(channelHandlerContext.channel().id(), protocolDataUnit));
    }

    private void startSession(final Channel clientChannel) {
        sessionMembers.putIfAbsent(clientChannel.id(), new LinkedList<>());

        lobbyServerFacade.findPlayerLobby(clientChannel.id()).ifPresent(playerLobby ->
            playerLobby.stream().filter(playerId -> !clientChannel.id().equals(playerId) && sessionMembers.containsKey(playerId)).findAny().ifPresent(playerId -> {
                final var messageQueue = new LinkedList<SessionMessage>();

                new Thread(managedSessions, () -> {
                    sessionMembers.put(clientChannel.id(), messageQueue);
                    sessionMembers.put(playerId, messageQueue);

                    final SessionChannelGroupFacade sessionChannelGroupFacade;

                    try {
                        sessionChannelGroupFacade = sessionChannelGroupFacadeFactory.get();
                        sessionChannelGroupFacade.receiveSessionStart(playerLobby.toArray(ChannelId[]::new));

                        while (!sessionChannelGroupFacade.isEnded()) {
                            final var sessionMessage = messageQueue.poll();

                            if(Objects.nonNull(sessionMessage) && sessionMessage.protocolDataUnit instanceof SessionPDU sessionPDU) {
                                if(sessionPDU.sessionFlag() == 1) {
                                }

                                if(sessionPDU.sessionFlag() == 2) {
                                    sessionChannelGroupFacade.receiveSessionEnd(playerLobby.toArray(ChannelId[]::new));
                                }
                            }

                            sessionChannelGroupFacade.receiveSessionTick(
                                    Objects.isNull(sessionMessage)? null : sessionMessage.playerId,
                                    playerLobby,
                                    Objects.isNull(sessionMessage) || !(sessionMessage.protocolDataUnit instanceof StateRequestPDU)? null : (StateRequestPDU) sessionMessage.protocolDataUnit,
                                    stateChannelGroupFacade
                            );

                            try {
                                TimeUnit.MILLISECONDS.sleep(33);
                            } catch (InterruptedException e) {
                                e.printStackTrace(); //todo: log4j
                            }
                        }
                        System.out.printf("Session with messageQueue: %s has ended\n", messageQueue); //todo: log4j
                        managedSessions.list();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                managedSessions.list();
            })
        );
    }

    private record SessionMessage(ChannelId playerId, PDU protocolDataUnit) {}
}
