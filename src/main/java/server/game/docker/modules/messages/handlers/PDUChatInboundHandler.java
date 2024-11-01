package server.game.docker.modules.messages.handlers;

import io.netty.channel.Channel;
import server.game.docker.GameServerInitializer;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.modules.messages.pdus.PDUChatMessage;
import server.game.docker.ship.parents.pdus.PDU;
import server.game.docker.GameServer;

public class PDUChatInboundHandler extends GameServerInitializer.PDUInboundHandler {
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
