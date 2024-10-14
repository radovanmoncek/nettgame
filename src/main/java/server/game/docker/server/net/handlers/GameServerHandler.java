package server.game.docker.server.net.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.net.modules.pdus.PDUID;
import server.game.docker.net.modules.pdus.PDULobbyReq;
import server.game.docker.net.pipelines.PDUMultiPipeline;
import server.game.docker.net.enums.PDUType;
import server.game.docker.server.GameServer;

import java.nio.ByteBuffer;

public class GameServerHandler extends ChannelInboundHandlerAdapter {
    private final GameServer gameServer;
    private final PDUMultiPipeline multiPipeline;
    private final LobbyPDUInboundHandler lobbyInboundHandler;

    public GameServerHandler(
            PDUMultiPipeline multiPipeline,
            GameServer gameServer, LobbyPDUInboundHandler lobbyInboundHandler
    ) {
        this.multiPipeline = multiPipeline;
        this.gameServer = gameServer;
        this.lobbyInboundHandler = lobbyInboundHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuffer byteBuffer = ((ByteBuffer) msg).position(0);
        multiPipeline.ingest(PDUType.valueOf(byteBuffer.get()), Unpooled.wrappedBuffer(byteBuffer), ctx.channel());
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

        multiPipeline.ingest(PDUType.ID, iD, ctx.channel());
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
        System.out.printf("Client %d has disconnected\n", gameServer.removeDebugChannelIDEntry(ctx.channel().id()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {}
}
