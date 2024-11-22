package server.game.docker.modules.session.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.lobby.facades.LobbyServerFacade;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.modules.session.facades.SessionServerFacade;
import server.game.docker.modules.session.pdus.SessionPDU;
import server.game.docker.modules.state.facades.StateServerFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SessionServerHandler extends SimpleChannelInboundHandler<PDU> {
    private final Supplier<SessionServerFacade> sessionServerFacadeFactory;
    private final LobbyServerFacade lobbyServerFacade;
    private final PlayerServerFacade playerServerFacade;
    private final StateServerFacade stateServerFacade;
    private static final Map<ChannelId, Queue<SessionMessage>> sessionMembers = new HashMap<>();

    public SessionServerHandler(
            final Supplier<SessionServerFacade> sessionServerFacadePrototype,
            final LobbyServerFacade lobbyServerFacade,
            final PlayerServerFacade playerServerFacade,
            final StateServerFacade stateServerFacade
    ) {
        this.sessionServerFacadeFactory = sessionServerFacadePrototype;
        this.lobbyServerFacade = lobbyServerFacade;
        this.playerServerFacade = playerServerFacade;
        this.stateServerFacade = stateServerFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PDU protocolDataUnit) {
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
        sessionMembers.putIfAbsent(clientChannel.id(), null);

        lobbyServerFacade.findPlayerLobby(clientChannel.id()).ifPresent(playerLobby ->
            playerLobby.stream().filter(playerId -> !clientChannel.id().equals(playerId) && sessionMembers.containsKey(playerId)).findAny().ifPresent(playerId -> {
                final var messageQueue = new LinkedList<SessionMessage>();

                new Thread(() -> {
                    sessionMembers.put(clientChannel.id(), messageQueue);
                    sessionMembers.put(playerId, messageQueue);
                    final SessionServerFacade sessionServerFacade;
                    try {
                        sessionServerFacade = sessionServerFacadeFactory.get();
                        sessionServerFacade.receiveSessionStart(playerLobby.toArray(ChannelId[]::new));
                        while (!sessionServerFacade.isEnded()) {
                            final var sessionMessage = messageQueue.poll();
                            if(Objects.nonNull(sessionMessage) && sessionMessage.protocolDataUnit instanceof SessionPDU sessionPDU) {
                                if(sessionPDU.sessionFlag() == 1) {
                                    continue;
                                }

                                if(sessionPDU.sessionFlag() == 2) {
                                    sessionServerFacade.receiveSessionEnd(playerLobby.toArray(ChannelId[]::new));
                                }
                            }

                            final var playerLobbyMap = new HashMap<ChannelId, String>();

                            playerLobby.forEach(channelId -> playerLobbyMap.put(channelId, playerServerFacade.getNickname(channelId)));

                            sessionServerFacade.receiveSessionTick(
                                    Objects.isNull(sessionMessage)? null : sessionMessage.playerId,
                                    playerLobbyMap,
                                    Objects.isNull(sessionMessage)? null : sessionMessage.protocolDataUnit,
                                    stateServerFacade
                            );

                            try {
                                TimeUnit.MILLISECONDS.sleep(33);
                            } catch (InterruptedException e) {
                                e.printStackTrace(); //todo: log4j
                            }
                        }
                        System.out.printf("Session with messageQueue: %s has ended", messageQueue); //todo: log4j
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            })
        );
    }

    private record SessionMessage(ChannelId playerId, PDU protocolDataUnit) {}
}