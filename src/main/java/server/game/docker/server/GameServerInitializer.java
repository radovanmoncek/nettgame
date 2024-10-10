package server.game.docker.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.modules.encoders.PDUStringEncoder;
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
    private final PDUMultiPipeline multiPipeline;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<ChannelId, Long> sessionDomain;
    private final Set<ChannelId> unassignedDomain;
    private final Map<Long, Lobby> lobbyLookup;
    private final ChannelGroup managedClients;
    private final GameServer gameServer;

    public GameServerInitializer(
            PDUMultiPipeline multiPipeline,
            Map<ChannelId, Long> lobbyDomain,
            Map<ChannelId, Long> sessionDomain,
            Set<ChannelId> unassignedDomain,
            Map<Long, Lobby> lobbyLookup,
            Map<ChannelId, Long> channelIDClientIDLookup,
            ChannelGroup managedClients, GameServer gameServer
    ) {
        this.multiPipeline = multiPipeline;
        this.lobbyDomain = lobbyDomain;
        this.sessionDomain = sessionDomain;
        this.unassignedDomain = unassignedDomain;
        this.lobbyLookup = lobbyLookup;
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
        multiPipeline.append(PDUType.ID, (PDUHandlerEncoder) (in, out) -> {
            ID id = (ID) in;
            ByteBuf byteBuf = Unpooled.buffer(1 + 2 * Long.BYTES)
                    .writeByte(PDUType.ID.ordinal())
                    .writeLong(Long.BYTES)
                    .writeLong(id.getNewClientID());
            out.writeAndFlush(byteBuf);
        })
        /*--------LOBBY--------*/
        //Inbound PDU no payload and action
        .append(PDUType.LOBBYREQUEST,
                (PDUHandlerDecoder) (byteBuf, handler) -> {
            LobbyReq lobbyReq = new LobbyReq();
            lobbyReq.setActionFlag(byteBuf.readByte());
            lobbyReq.setLobbyID(byteBuf.readLong());
            handler.handle(lobbyReq);
        }, lobbyPDUInboundHandler)
                .append(PDUType.LOBBYUPDATE,
                        (PDUHandlerEncoder) (in, channel) -> {
                            LobbyUpdate lobbyUpdate = (LobbyUpdate) in;
                            ByteBuf byteBuf = Unpooled.buffer(1 + Long.BYTES)
                                    .writeByte(PDUType.LOBBYUPDATE.ordinal())
                                    .writeLong(1 + Long.BYTES + 1 + lobbyUpdate.getMembers().size() * (long) Long.BYTES)
                                    .writeByte(lobbyUpdate.getStateFlag())
                                    .writeLong(lobbyUpdate.getLobbyId())
                                    .writeBoolean(lobbyUpdate.isLeader());

                            lobbyUpdate.getMembers().forEach(byteBuf::writeLong);

                            channel.writeAndFlush(byteBuf);
                        })
        //Outbound only PDU with no action, 8B Long, 2 * 1B Byte data and 1B Byte - Boolean Lobby list refresh flag
        .append(PDUType.LOBBYBEACON, (PDUHandlerEncoder) (in, out) -> {
            LobbyBeacon lobbyBeacon = (LobbyBeacon) in;
            ByteBuf byteBuf = Unpooled.buffer(Long.BYTES + 3 * Byte.BYTES)
                    .writeLong(lobbyBeacon.getLobbyID())
                    .writeByte(lobbyBeacon.getLobbyCurOccupancy())
                    .writeByte(lobbyBeacon.getLobbyMaxOccupancy())
                    .writeBoolean(lobbyBeacon.getLobbyListRefresh());

            out.writeAndFlush(byteBuf);
        })
                .append(PDUType.CHATMESSAGE, new PDUStringEncoder());
    }
}
