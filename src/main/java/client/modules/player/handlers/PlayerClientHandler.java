package client.modules.player.handlers;

import client.ship.parents.handlers.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import server.game.docker.modules.player.pdus.NicknameProtocolDataUnit;

public class PlayerClientHandler extends ChannelHandler<NicknameProtocolDataUnit> {

    /**
     * Returns a nickname that was assigned to this client by the DOcker Game Server.
     * @param ctx           the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                      belongs to
     * @param msg           the message to handle
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NicknameProtocolDataUnit msg) {}

    /**
     * Sends a {@link NicknameProtocolDataUnit} request to the Docker Game Server.
     * @param newNickname the requested nickname.
     * @throws IllegalArgumentException if the requested nickname length exceeds 8 characters.
     */
    public void requestNickname(final String newNickname) {
        if (newNickname.length() > 8)
            throw new IllegalArgumentException("Player name's length exceeds 8 characters");

        final var nicknamePDU = new NicknameProtocolDataUnit(newNickname);

        unicastPDUToServerChannel(nicknamePDU);
    }
}
