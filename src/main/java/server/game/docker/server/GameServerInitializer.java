package server.game.docker.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.net.modules.decoders.PDUChatMessageDecoder;
import server.game.docker.net.modules.encoders.PDUChatMessageEncoder;
import server.game.docker.net.modules.pdus.PDUID;
import server.game.docker.net.parents.handlers.PDUHandler;
import server.game.docker.net.pipelines.PDUMultiPipeline;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.pdus.PDULobbyBeacon;
import server.game.docker.net.modules.pdus.PDULobbyReq;
import server.game.docker.net.modules.pdus.PDULobbyUpdate;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.server.net.handlers.LobbyPDUInboundHandler;
import server.game.docker.server.net.handlers.LobbyPDUInboundHandler.Lobby;
import server.game.docker.server.net.handlers.PDUChatInboundHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameServerInitializer {
    private final PDUMultiPipeline multiPipeline;
    private final GameServer gameServer;
    private final PDUHandler lobbyPDUInboundHandler;

    public GameServerInitializer(
            PDUMultiPipeline multiPipeline,
            GameServer gameServer, PDUHandler lobbyPDUInboundHandler
    ) {
        this.multiPipeline = multiPipeline;
        this.gameServer = gameServer;
        this.lobbyPDUInboundHandler = lobbyPDUInboundHandler;
    }

    public void init() {
        /*--------ID - handshake with server--------*/
        //Outbound only PDU containing 64-bit Long clientID
        multiPipeline.append(PDUType.ID, (PDUHandlerEncoder) (in, out) -> {
                    PDUID PDUID = (PDUID) in;
                    ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES)
                            .writeByte(PDUType.ID.oneBasedOrdinal())
                            .writeLong(Long.BYTES)
                            .writeLong(PDUID.getNewClientID());
                    out.writeAndFlush(byteBuf);
                })
                /*--------LOBBY--------*/
                //Inbound PDU no payload and action
                .append(PDUType.LOBBYREQUEST,
                        (PDUHandlerDecoder) (byteBuf, channel, handler) -> {
                            PDULobbyReq PDULobbyReq = new PDULobbyReq();
                            PDULobbyReq.setActionFlag(byteBuf.readByte());
                            PDULobbyReq.setLobbyID(byteBuf.readLong());
                            handler.handle(PDULobbyReq, channel);
                        }, lobbyPDUInboundHandler)
                .append(PDUType.LOBBYUPDATE,
                        (PDUHandlerEncoder) (in, channel) -> {
                            PDULobbyUpdate lobbyUpdate = (PDULobbyUpdate) in;
                            ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                                    .writeByte(PDUType.LOBBYUPDATE.oneBasedOrdinal())
                                    .writeLong(Long.BYTES + 2 * Byte.BYTES + (lobbyUpdate.getMembers() == null? 0 : lobbyUpdate.getMembers().size() * (long) Long.BYTES))
                                    .writeByte(lobbyUpdate.getStateFlag())
                                    .writeLong(lobbyUpdate.getLobbyId() == null? -1L : lobbyUpdate.getLobbyId())
                                    .writeBoolean(lobbyUpdate.isLeader() != null && lobbyUpdate.isLeader());

                            (lobbyUpdate.getMembers() == null? new ArrayList<Long>() : lobbyUpdate.getMembers()).forEach(byteBuf::writeLong);

                            channel.writeAndFlush(byteBuf);
                        })
                //Outbound only PDU with no action, 8B Long, 2 * 1B Byte data and 1B Byte - Boolean Lobby list refresh flag
                .append(PDUType.LOBBYBEACON, (PDUHandlerEncoder) (in, out) -> {
                    PDULobbyBeacon lobbyBeacon = (PDULobbyBeacon) in;
                    ByteBuf byteBuf = Unpooled.buffer(2 * Long.BYTES + 4 * Byte.BYTES)
                            .writeByte(PDUType.LOBBYBEACON.oneBasedOrdinal())
                            .writeLong(Long.BYTES + 2 * Byte.BYTES + 1)
                            .writeLong(lobbyBeacon.getLobbyID())
                            .writeByte(lobbyBeacon.getLobbyCurOccupancy())
                            .writeByte(lobbyBeacon.getLobbyMaxOccupancy())
                            .writeBoolean(lobbyBeacon.getLobbyListRefresh());

                    out.writeAndFlush(byteBuf);
                })
                .append(PDUType.CHATMESSAGE, new PDUChatMessageEncoder(), new PDUChatMessageDecoder(), new PDUChatInboundHandler(gameServer));
    }
}
