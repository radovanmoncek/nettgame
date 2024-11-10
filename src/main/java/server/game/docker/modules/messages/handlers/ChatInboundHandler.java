package server.game.docker.modules.messages.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.messages.pdus.ChatMessagePDU;

public final class ChatInboundHandler extends SimpleChannelInboundHandler<ChatMessagePDU> {
    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, ChatMessagePDU chatMessagePDU) {
    }
}
