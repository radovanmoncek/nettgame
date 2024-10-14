package server.game.docker.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.decoders.PDUChatMessageDecoder;
import server.game.docker.net.modules.encoders.PDUChatMessageEncoder;
import server.game.docker.net.modules.pdus.PDUID;
import server.game.docker.net.modules.pdus.PDULobbyBeacon;
import server.game.docker.net.modules.pdus.PDULobbyReq;
import server.game.docker.net.modules.pdus.PDULobbyUpdate;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.pipelines.PDUMultiPipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ClientInitializer {
    private final GameSessionClient gameSessionClient;
    private final PDUMultiPipeline multiPipeline;

    public ClientInitializer(Channel clientChannel, Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings, GameSessionClient gameSessionClient, PDUMultiPipeline multiPipeline) {
        this.gameSessionClient = gameSessionClient;
        this.multiPipeline = multiPipeline;
    }

    public void init() {
        multiPipeline.append(PDUType.ID,
                        (PDUHandlerDecoder) (in, channel, out) -> {
                            PDUID identifier = new PDUID();
                            identifier.setNewClientID(in.readLong());
                            out.handle(identifier);
                        }, new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameSessionClient.checkAndCallHandler(ClientAPIEventType.CONNECTED, in);
                            }
                        })
                .append(PDUType.LOBBYREQUEST,
                        (PDUHandlerEncoder) (in, out) -> {
                            PDULobbyReq PDULobbyReq = (PDULobbyReq) in;
                            ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + Long.BYTES)
                                    .writeByte(PDUType.LOBBYREQUEST.oneBasedOrdinal())
                                    .writeLong(Byte.BYTES + Long.BYTES)
                                    .writeByte(PDULobbyReq.getActionFlag() == 0 ? 0 : PDULobbyReq.getActionFlag() == 1 ? 1 : 2); //Warcrime for the greater good
                            if (PDULobbyReq.getActionFlag() == 1) {
                                byteBuf.writeLong(PDULobbyReq.getLobbyID());
                            }

                            out.writeAndFlush(byteBuf);
                        })
                .append(PDUType.LOBBYUPDATE,
                        (PDUHandlerDecoder) (in, channel, out) -> {
                            PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
                            lobbyUpdate.setStateFlag((byte) in.readUnsignedByte());
                            lobbyUpdate.setLobbyId(in.readLong());
                            lobbyUpdate.setLeader(in.readBoolean());
                            final Collection<Long> lobbyMembers = new ArrayList<>();
                            while (in.readableBytes() >= 8) {
                                if(in.readLong() == 0)
                                    break;
                                lobbyMembers.add(in.readLong());
                            }
                            lobbyUpdate.setMembers(lobbyMembers);

                            out.handle(lobbyUpdate);
                        }, new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                switch (((PDULobbyUpdate) in).getStateFlag()) {
                                    case 0 ->
                                            gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYCREATED, in);
                                    case 1 -> gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYJOINED, in);
                                    case 2 -> gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYLEFT, in);
                                    case 3 -> gameSessionClient.checkAndCallHandler(ClientAPIEventType.MEMBERJOINED, in);
                                    case 4 -> gameSessionClient.checkAndCallHandler(ClientAPIEventType.MEMBERLEFT, in);
                                }
                            }
                        })
                .append(PDUType.LOBBYBEACON,
                        (PDUHandlerDecoder) (in, channel, out) -> {
                            PDULobbyBeacon PDULobbyBeacon = new PDULobbyBeacon();
                            PDULobbyBeacon.setLobbyID(in.readLong());
                            PDULobbyBeacon.setLobbyCurOccupancy(in.readByte());
                            PDULobbyBeacon.setLobbyMaxOccupancy(in.readByte());
                            PDULobbyBeacon.setLobbyListRefresh(in.readBoolean());
                            out.handle(PDULobbyBeacon);
                        }, new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYBEACON, in);
                            }
                        })
                .append(PDUType.CHATMESSAGE,
                        new PDUChatMessageEncoder(),
                        new PDUChatMessageDecoder(),
                        new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                            }

                            @Override
                            public void handle(PDU in, Channel channel) {
                                gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYCHATMESSAGERECEIVED, in);
                            }
                        }
                );
    }
}
