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
    private static final Map<ChannelId, Integer> sessionMembers = new HashMap<>();
    private static final ArrayList<Queue<SessionMessage>> sessionMessageQueues = new ArrayList<>();

    public SessionServerHandler(Supplier<SessionServerFacade> sessionServerFacadePrototype, LobbyServerFacade lobbyServerFacade, PlayerServerFacade playerServerFacade, StateServerFacade stateServerFacade) {
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
                    final var sessionMessageQueueIndex = sessionMembers.remove(channelHandlerContext.channel().id());
                    final var lobby = lobbyServerFacade.findPlayerLobby(channelHandlerContext.channel().id());
                    lobby
                            .stream()
                            .filter(playerId -> !playerId.equals(channelHandlerContext.channel().id()) && !sessionMembers.containsKey(playerId))
                            .findAny()
                            .ifPresent(playerId -> sessionMessageQueues.get(sessionMessageQueueIndex).offer(new SessionMessage(channelHandlerContext.channel().id(), sessionPDU)));
                }
                case 1 ->{
                    //TODO:
                }
            }

            return;
        }

        sessionMessageQueues.get(sessionMembers.get(channelHandlerContext.channel().id())).offer(new SessionMessage(channelHandlerContext.channel().id(), protocolDataUnit));
    }

    private void startSession(final Channel clientChannel) {
        sessionMembers.putIfAbsent(clientChannel.id(), null);

        LinkedList<ChannelId> playerLobby;
        if ((playerLobby = lobbyServerFacade.findPlayerLobby(clientChannel.id())) != null) {
            playerLobby.stream().filter(playerId -> !clientChannel.id().equals(playerId) && sessionMembers.containsKey(playerId)).findAny().ifPresent(playerId -> {
                sessionMessageQueues.add(new LinkedList<>());

                new Thread(() -> {
                    final var messageQueueIndex = sessionMessageQueues.size() - 1;
                    sessionMembers.put(clientChannel.id(), messageQueueIndex);
                    sessionMembers.put(playerId, messageQueueIndex);
                    final SessionServerFacade sessionServerFacade;
                    try {
                        sessionServerFacade = sessionServerFacadeFactory.get();
                        sessionServerFacade.receiveSessionStart(playerLobby.toArray(ChannelId[]::new));
                        while (!sessionServerFacade.isEnded()) {
                            final var sessionMessage = pollMessageQueue(messageQueueIndex);
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
                        System.out.printf("Session with messageQueue index %d has ended", messageQueueIndex); //todo: log4j
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        }
    }

    private synchronized SessionMessage pollMessageQueue(final Integer messageQueueIndex) {
        return sessionMessageQueues.get(messageQueueIndex).poll();
    }

    private record SessionMessage(ChannelId playerId, PDU protocolDataUnit) {}
}