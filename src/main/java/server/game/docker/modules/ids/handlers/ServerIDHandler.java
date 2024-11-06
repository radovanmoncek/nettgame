package server.game.docker.modules.ids.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.GameServer;
import server.game.docker.modules.ids.pdus.PDUID;

public class ServerIDHandler extends SimpleChannelInboundHandler<PDUID> {
    private final GameServer gameServer;

    public ServerIDHandler(
            GameServer gameServer
    ) {
        this.gameServer = gameServer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, PDUID id) {

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        gameServer.startManagingClient(ctx.channel());
        gameServer.addToUnassigned(ctx.channel().id());

        System.out.println("A client has requested an ID");

        //Body data payload is empty for this PDUType
        final var iD = new PDUID();
        iD.setNewClientID(gameServer.getNextDebugClientID());

        System.out.printf("A client has connected with assigned ID: %d\n", iD.getNewClientID()); //todo: log4j

        gameServer.putDebugChannelIDtoLongMapping(ctx.channel().id(), iD.getNewClientID());
        ctx.channel().writeAndFlush(iD);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        gameServer.unassignChannel(ctx.channel().id());
        gameServer.stopManagingChannel(ctx.channel()); //todo: redundant? Netty seems to automatically unregister channels from ChannelGroup on UNREGISTER
        //todo: handle session leave
        System.out.printf("Client %d has disconnected\n", gameServer.removeDebugChannelIDEntry(ctx.channel().id())); //todo: log4j
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(cause.getMessage());
    }
}
