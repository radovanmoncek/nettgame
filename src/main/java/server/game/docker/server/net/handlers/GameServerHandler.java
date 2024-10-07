package server.game.docker.server.net.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.LocalPDUPipeline;
import server.game.docker.net.dto.IDRes;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;
import server.game.docker.server.GameServer;
import server.game.docker.server.net.handlers.LobbyPDUInboundHandler.Lobby;

import java.util.Map;
import java.util.Set;

public class GameServerHandler extends ChannelInboundHandlerAdapter {
    private final Map<ChannelId, Long> channelIDClientIDLookup;
    private final Map<PDUType, LocalPDUPipeline> localPDUPipelines;
    private final ChannelGroup managedClients;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<Long, Lobby> lobbyLookup;
    private final Map<ChannelId, Long> sessionDomain;
    private final Set<ChannelId> unassignedDomain;
    private final GameServer gameServer;

    public GameServerHandler(
            Map<ChannelId, Long> channelIDClientIDLookup,
            Map<PDUType, LocalPDUPipeline> localPDUPipelines,
            ChannelGroup managedClients,
            Map<ChannelId, Long> lobbyDomain,
            Map<Long, Lobby> lobbyLookup,
            Map<ChannelId, Long> sessionDomain,
            Set<ChannelId> unassignedDomain,
            GameServer gameServer
    ) {
        this.channelIDClientIDLookup = channelIDClientIDLookup;
        this.localPDUPipelines = localPDUPipelines;
        this.managedClients = managedClients;
        this.lobbyDomain = lobbyDomain;
        this.lobbyLookup = lobbyLookup;
        this.sessionDomain = sessionDomain;
        this.unassignedDomain = unassignedDomain;
        this.gameServer = gameServer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        PDU pdu = (PDU) msg;
        localPDUPipelines.get(pdu.getPDUType()).ingest(pdu);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        managedClients.add(ctx.channel());
        unassignedDomain.add(ctx.channel().id());

        System.out.println("A client has requested an ID");

        //Body data payload is empty for this PDUType
        IDRes idRes = new IDRes();
        idRes.setNewClientID(gameServer.getNextDebugClientID());

        PDU pdu = new PDU();
        pdu.setPDUType(PDUType.IDRES);
        pdu.setAddress(ctx.channel().remoteAddress());
        pdu.setData(idRes);

        System.out.printf("A client has connected with assigned ID: %d\n", idRes.getNewClientID());

        channelIDClientIDLookup.put(ctx.channel().id(), idRes.getNewClientID());

        localPDUPipelines.get(pdu.getPDUType()).ingest(pdu, ctx.channel());
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
