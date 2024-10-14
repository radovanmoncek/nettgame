package server.game.docker.server.net.handlers;

import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.pdus.PDUChatMessage;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.server.GameServer;

public class PDUChatInboundHandler extends PDUInboundHandler {
    private final GameServer gameServer;

    public PDUChatInboundHandler(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    @Override
    public void handle(PDU in) {
    }

    @Override
    public void handle(PDU in, Channel channel) {
        if(gameServer.hasAnyLobbies() && gameServer.findLobbyAssignedChannel(channel) == null)
            return;

        Long lobbyID = gameServer.lookupLobbyIDForChannelID(channel.id());

        PDUChatMessage chatMessage = new PDUChatMessage();
        chatMessage.setAuthorName(gameServer.transformChID(channel.id()).toString());
        chatMessage.setMessage(((PDUChatMessage) in).getMessage());

        gameServer.sendMulticastLobby(PDUType.CHATMESSAGE, chatMessage, channel);
    }
}
