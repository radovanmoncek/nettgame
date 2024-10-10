package server.game.docker.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.encoders.PDUStringEncoder;
import server.game.docker.net.modules.pdus.ID;
import server.game.docker.net.modules.pdus.LobbyBeacon;
import server.game.docker.net.modules.pdus.LobbyReq;
import server.game.docker.net.modules.pdus.LobbyUpdate;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.pipelines.PDUMultiPipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
                        (PDUHandlerDecoder) (in, out) -> {
                            ID identifier = new ID();
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
                            LobbyReq lobbyReq = (LobbyReq) in;
                            ByteBuf byteBuf = Unpooled.buffer(1 + Long.BYTES)
                                    .writeByte(PDUType.LOBBYREQUEST.ordinal())
                                    .writeLong(1 + Long.BYTES)
                                    .writeByte(lobbyReq.getActionFlag() == 0 ? 0 : lobbyReq.getActionFlag() == 1 ? 1 : 2); //Warcrime for the greater good
                            if (lobbyReq.getActionFlag() == 1) {
                                byteBuf.writeLong(lobbyReq.getLobbyID());
                            }

                            out.writeAndFlush(byteBuf);
                        })
                .append(PDUType.LOBBYUPDATE,
                        (PDUHandlerDecoder) (in, out) -> {
                            LobbyUpdate lobbyUpdate = new LobbyUpdate();
                            lobbyUpdate.setLobbyId(in.readLong());
                            lobbyUpdate.setLeader(in.readBoolean());
                            final Collection<Long> lobbyMembers = new ArrayList<>();
                            while ((in.writerIndex() - in.readerIndex()) >= 4) {
                                lobbyMembers.add(in.readLong());
                            }
                            lobbyUpdate.setMembers(lobbyMembers);

                            out.handle(lobbyUpdate);
                        }, new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                switch (((LobbyUpdate) in).getStateFlag()) {
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
                        (PDUHandlerDecoder) (in, out) -> {
                            LobbyBeacon lobbyBeacon = new LobbyBeacon();
                            lobbyBeacon.setLobbyID(in.readLong());
                            lobbyBeacon.setLobbyCurOccupancy(in.readByte());
                            lobbyBeacon.setLobbyMaxOccupancy(in.readByte());
                            lobbyBeacon.setLobbyListRefresh(in.readBoolean());
                            out.handle(lobbyBeacon);
                        }, new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYBEACON, in);
                            }
                        })
                .append(PDUType.CHATMESSAGE,
                        new PDUStringEncoder(),
                        new PDUStringEncoder(),
                        new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYCHATMESSAGERECEIVED, in);
                            }
                        }
                );
    }
}
