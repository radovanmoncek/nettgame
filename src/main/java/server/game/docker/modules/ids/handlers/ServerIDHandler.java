package server.game.docker.modules.ids.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.GameServerInitializer;
import server.game.docker.modules.ids.pdus.PDUID;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.GameServer;
import server.game.docker.modules.requests.handlers.PDULobbyInboundHandler;

import java.nio.ByteBuffer;

public class ServerIDHandler extends ChannelInboundHandlerAdapter {
    private final GameServer gameServer;
    private final GameServerInitializer.RouterHandler multiPipeline;
    private final PDULobbyInboundHandler lobbyInboundHandler;

    public ServerIDHandler(
            GameServerInitializer.RouterHandler multiPipeline,
            GameServer gameServer, PDULobbyInboundHandler lobbyInboundHandler
    ) {
        this.multiPipeline = multiPipeline;
        this.gameServer = gameServer;
        this.lobbyInboundHandler = lobbyInboundHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuffer byteBuffer = ((ByteBuffer) msg).position(0);
        multiPipeline.route(PDUType.valueOf(byteBuffer.get()), Unpooled.wrappedBuffer(byteBuffer), ctx.channel());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        gameServer.startManagingClient(ctx.channel());
        gameServer.addToUnassigned(ctx.channel().id());

        System.out.println("A client has requested an ID");

        //Body data payload is empty for this PDUType
        PDUID iD = new PDUID();
        iD.setNewClientID(gameServer.getNextDebugClientID());

        System.out.printf("A client has connected with assigned ID: %d\n", iD.getNewClientID()); //todo: log4j

        gameServer.putDebugChannelIDtoLongMapping(ctx.channel().id(), iD.getNewClientID());

        multiPipeline.route(PDUType.ID, iD, ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        PDULobbyReq lobbyReq = new PDULobbyReq();
        lobbyReq.setActionFlag(PDULobbyReq.LEAVE);
        // todo: handle
        lobbyInboundHandler.handle(lobbyReq, ctx.channel());
        gameServer.unassignChannel(ctx.channel().id());
        gameServer.stopManagingChannel(ctx.channel()); //todo: redundant? Netty seems to automatically unregister channels from ChannelGroup on UNREGISTER
        //todo: handle session leave
        System.out.printf("Client %d has disconnected\n", gameServer.removeDebugChannelIDEntry(ctx.channel().id())); //todo: log4j
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {}
}
