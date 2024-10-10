package server.game.docker.server.net.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.modules.pdus.LobbyUpdate;
import server.game.docker.net.pipelines.PDUMultiPipeline;
import server.game.docker.net.modules.pdus.ID;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.enums.PDUType;
import server.game.docker.server.GameServer;
import server.game.docker.server.net.handlers.LobbyPDUInboundHandler.Lobby;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class GameServerHandler extends ChannelInboundHandlerAdapter {
    private final Map<ChannelId, Long> channelIDClientIDLookup;
    private final PDUMultiPipeline multiPipeline;
    private final ChannelGroup managedClients;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<Long, Lobby> lobbyLookup;
    private final Map<ChannelId, Long> sessionDomain;
    private final Set<ChannelId> unassignedDomain;
    private final GameServer gameServer;

    public GameServerHandler(
            Map<ChannelId, Long> channelIDClientIDLookup,
            PDUMultiPipeline multiPipeline,
            ChannelGroup managedClients,
            Map<ChannelId, Long> lobbyDomain,
            Map<Long, Lobby> lobbyLookup,
            Map<ChannelId, Long> sessionDomain,
            Set<ChannelId> unassignedDomain,
            GameServer gameServer
    ) {
        this.channelIDClientIDLookup = channelIDClientIDLookup;
        this.multiPipeline = multiPipeline;
        this.managedClients = managedClients;
        this.lobbyDomain = lobbyDomain;
        this.lobbyLookup = lobbyLookup;
        this.sessionDomain = sessionDomain;
        this.unassignedDomain = unassignedDomain;
        this.gameServer = gameServer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuffer byteBuffer = (ByteBuffer) msg;
        multiPipeline.ingest(PDUType.valueOf(byteBuffer.get()), Unpooled.wrappedBuffer(byteBuffer));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        managedClients.add(ctx.channel());
        unassignedDomain.add(ctx.channel().id());

        System.out.println("A client has requested an ID");

        //Body data payload is empty for this PDUType
        ID iD = new ID();
        iD.setNewClientID(gameServer.getNextDebugClientID());

        System.out.printf("A client has connected with assigned ID: %d\n", iD.getNewClientID()); //todo: log4j

        channelIDClientIDLookup.put(ctx.channel().id(), iD.getNewClientID());

        multiPipeline.ingest(PDUType.ID, iD, ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        managedClients.remove(ctx.channel());
        unassignedDomain.remove(ctx.channel().id());
        lobbyLookup.remove(lobbyDomain.remove(ctx.channel().id())); //todo: handle
        sessionDomain.remove(ctx.channel().id()); //todo: handle
        System.out.printf("Client %d has disconnected\n", channelIDClientIDLookup.remove(ctx.channel().id()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {}
}
