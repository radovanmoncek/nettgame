package server.game.docker.client.modules.ids.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.GameClient;
import server.game.docker.modules.ids.pdus.PDUID;

public class ClientIDHandler extends SimpleChannelInboundHandler<PDUID> {

    private final GameClient gameClient;

    public ClientIDHandler(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PDUID msg) {
        gameClient.setAssignedID(msg.getNewClientID());
    }
}
