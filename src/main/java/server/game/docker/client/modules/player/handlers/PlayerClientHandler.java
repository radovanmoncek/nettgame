package server.game.docker.client.modules.player.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.player.facades.PlayerChannelFacade;
import server.game.docker.modules.player.pdus.NicknamePDU;

public class PlayerClientHandler extends SimpleChannelInboundHandler<NicknamePDU> {
    private final PlayerChannelFacade playerClientFacade;

    public PlayerClientHandler(final PlayerChannelFacade playerClientFacade) {
        this.playerClientFacade = playerClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NicknamePDU msg) {
        playerClientFacade.receiveNewNickname(msg.nickname());
    }
}
