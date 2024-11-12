package server.game.docker.modules.chat.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;

public final class ChatInboundHandler extends SimpleChannelInboundHandler<ChatMessagePDU> {
    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, ChatMessagePDU chatMessagePDU) {
    }
}
