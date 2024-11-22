package server.game.docker.client.modules.messages.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.messages.facades.ChatMessageClientFacade;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;

public class ChatMessageClientHandler extends SimpleChannelInboundHandler<ChatMessagePDU> {
    private final ChatMessageClientFacade chatMessageClientFacade;

    public ChatMessageClientHandler(ChatMessageClientFacade chatMessageClientFacade) {
        this.chatMessageClientFacade = chatMessageClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessagePDU msg) {
        System.out.printf("Player lobby message received %s\n", msg);
        chatMessageClientFacade.receivePlayerLobbyChatMessage(msg.authorNick(), msg.message());
    }
}
