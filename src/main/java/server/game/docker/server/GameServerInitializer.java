package server.game.docker.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.pipelines.PDUMultiPipeline;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.pdus.ID;
import server.game.docker.net.modules.pdus.LobbyBeacon;
import server.game.docker.net.modules.pdus.LobbyReq;
import server.game.docker.net.modules.pdus.LobbyUpdate;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.server.net.handlers.LobbyPDUInboundHandler;
import server.game.docker.server.net.handlers.LobbyPDUInboundHandler.Lobby;

import java.util.Map;
import java.util.Set;

public class GameServerInitializer {
    private final Map<PDUType, PDUMultiPipeline> localPDUPipelines;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<ChannelId, Long> sessionDomain;
    private final Set<ChannelId> unassignedDomain;
    private final Map<Long, Lobby> lobbyLookup;
    private final Map<ChannelId, Long> channelIDClientIDLookup;
    private final ChannelGroup managedClients;
    private final GameServer gameServer;

    public GameServerInitializer(
            PDUMultiPipeline localPDUPipelines,
            Map<ChannelId, Long> lobbyDomain,
            Map<ChannelId, Long> sessionDomain,
            Set<ChannelId> unassignedDomain,
            Map<Long, Lobby> lobbyLookup,
            Map<ChannelId, Long> channelIDClientIDLookup,
            ChannelGroup managedClients, GameServer gameServer
    ) {
        this.localPDUPipelines = localPDUPipelines;
        this.lobbyDomain = lobbyDomain;
        this.sessionDomain = sessionDomain;
        this.unassignedDomain = unassignedDomain;
        this.lobbyLookup = lobbyLookup;
        this.channelIDClientIDLookup = channelIDClientIDLookup;
        this.managedClients = managedClients;
        this.gameServer = gameServer;
    }

    public void init() {
        final LobbyPDUInboundHandler lobbyPDUInboundHandler = new LobbyPDUInboundHandler(
                gameServer,
                lobbyLookup,
                unassignedDomain,
                managedClients,
                lobbyDomain,
                sessionDomain
        );
        /*--------ID - handshake with server--------*/
        //Outbound only PDU containing 64-bit Long clientID
        localPDUPipelines.put(PDUType.ID, new PDUMultiPipeline().append(new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                ID id = (ID) in.getData();
                in.setData(Unpooled.buffer(Long.BYTES).writeLong(id.getNewClientID()));
                out.writeAndFlush(in);
            }
        }));
        /*--------LOBBY--------*/
        //Inbound PDU no payload and action
        localPDUPipelines.put(PDUType.CREATELOBBYREQ, new PDUMultiPipeline().append(lobbyPDUInboundHandler));
        //Outbound only PDU 32-bit Integer payload
        localPDUPipelines.put(PDUType.CREATELOBBYRES, new PDUMultiPipeline().append(new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                LobbyUpdate lobbyUpdate = (LobbyUpdate) in.getData();
                in.setData(Unpooled.buffer(Long.BYTES).writeLong(lobbyUpdate.getLobbyId()));
                out.writeAndFlush(in);
            }
        }));
        //Inbound only PDU with 8B Long data and action
        localPDUPipelines.put(PDUType.JOINLOBBYREQ, new PDUMultiPipeline().append(new PDUHandlerDecoder() {
            @Override
            public void decode(PDU in, PDUInboundHandler out) {
                LobbyReq lobbyReq = new LobbyReq();
                lobbyReq.setLobbyID(((ByteBuf) in.getData()).readLong());
                in.setData(lobbyReq);
                out.handle(in);
            }}, lobbyPDUInboundHandler));
        //Outbound only PDU no action with 8B Long data
        localPDUPipelines.put(PDUType.JOINLOBBYRES, new PDUMultiPipeline().append(new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                LobbyReq.LobbyRes joinLobbyRes = (LobbyReq.LobbyRes) in.getData();
                in.setData(Unpooled.buffer(Long.BYTES).writeLong(joinLobbyRes.getLobbyID()));
                out.writeAndFlush(in);
            }
        }));
        //Inbound PDU with no payload and with action
        localPDUPipelines.put(PDUType.LEAVELOBBYREQ, new PDUMultiPipeline().append(lobbyPDUInboundHandler));
        //Outbound PDU with 1B Boolean data or action
        localPDUPipelines.put(PDUType.LEAVELOBBYRES, new PDUMultiPipeline().append(new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                LobbyUpdate.LeaveLobbyRes leaveLobbyRes = (LobbyUpdate.LeaveLobbyRes) in.getData();
                in.setData(Unpooled.buffer(1).writeBoolean(leaveLobbyRes.isLeader()));
                out.writeAndFlush(in);
            }
        }));
        //Outbound only PDU with no action, 8B Long, 2 * 1B Byte data and 1B Byte - Boolean Lobby list refresh flag
        localPDUPipelines.put(PDUType.LOBBYBEACON, new PDUMultiPipeline().append(new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                LobbyBeacon lobbyBeacon = (LobbyBeacon) in.getData();
                in.setData(Unpooled.buffer(Long.BYTES + 3 * Byte.BYTES).writeLong(lobbyBeacon.getLobbyID()).writeByte(lobbyBeacon.getLobbyCurOccupancy()).writeByte(lobbyBeacon.getLobbyMaxOccupancy()).writeBoolean(lobbyBeacon.getLobbyListRefresh()));
                out.writeAndFlush(in);
            }
        }));
    }
}
