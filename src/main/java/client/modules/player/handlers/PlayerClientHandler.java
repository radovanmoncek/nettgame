package client.modules.player.handlers;

import client.ship.parents.facades.ChannelPDUCommunicationsHandler;
import io.netty.channel.ChannelHandlerContext;
import server.game.docker.modules.player.pdus.NicknamePDU;

public class PlayerClientHandler extends ChannelPDUCommunicationsHandler<NicknamePDU> {

    /**
     * Returns a nickname that was assigned to this client by the DOcker Game Server.
     * @param ctx           the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                      belongs to
     * @param msg           the message to handle
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NicknamePDU msg) {}

    /**
     * Sends a {@link NicknamePDU} request to the Docker Game Server.
     * @param newNickname the requested nickname.
     * @throws IllegalArgumentException if the requested nickname length exceeds 8 characters.
     */
    public void requestNickname(final String newNickname) {
        if (newNickname.length() > 8)
            throw new IllegalArgumentException("Player name's length exceeds 8 characters");

        final var nicknamePDU = new NicknamePDU(newNickname);

        unicastPDUToServerChannel(nicknamePDU);
    }
}
