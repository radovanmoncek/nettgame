package server.game.docker.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.encoders.PDUStringEncoder;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.LocalPDUPipeline;
import server.game.docker.net.dto.*;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.util.Map;

public class ClientInitializer {
    private final Channel clientChannel;
    private final Map<PDUType, LocalPDUPipeline> localPDUPipelines;
    private final Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings;
    private final GameSessionClient gameSessionClient;

    public ClientInitializer(Channel clientChannel, Map<PDUType, LocalPDUPipeline> localPDUPipelines, Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings, GameSessionClient gameSessionClient) {
        this.clientChannel = clientChannel;
        this.localPDUPipelines = localPDUPipelines;
        this.eventMappings = eventMappings;
        this.gameSessionClient = gameSessionClient;
    }

    public void init() {
        //Inbound only PDU with action
        localPDUPipelines.put(PDUType.IDRES, new LocalPDUPipeline().append(
                new PDUHandlerDecoder() {
                    @Override
                    public void decode(PDU in, PDUInboundHandler out) {
                        IDRes iDres = new IDRes();
                        iDres.setNewClientID(((ByteBuf) in.getData()).readLong());
                        in.setData(iDres);
                        out.handle(in);
                    }
                    }, new PDUInboundHandler() {
        @Override
            public void handle(PDU p) {
                gameSessionClient.checkAndGetHandler(IDRes.class, ClientAPIEventType.CONNECTED).handle((IDRes) p.getData());
            }
        }));
        //Outbound PDU with no payload
        localPDUPipelines.put(PDUType.CREATELOBBYREQ, new LocalPDUPipeline().append(
                new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                in.setData(Unpooled.buffer(0));
                out.writeAndFlush(in);
            }
        }));
        //Inbound PDU with 32-bit Integer payload and required action
        localPDUPipelines.put(PDUType.CREATELOBBYRES, new LocalPDUPipeline().append(
                new PDUHandlerDecoder() {
            @Override
            public void decode(PDU in, PDUInboundHandler out) {
                CreateLobbyRes createLobbyRes = new CreateLobbyRes();
                createLobbyRes.setLobbyId(((ByteBuf) in.getData()).readLong());
                in.setData(createLobbyRes);
                out.handle(in);
            }}, new PDUInboundHandler(){
            @Override
            public void handle(PDU p) {
                CreateLobbyRes createLobbyRes = (CreateLobbyRes) p.getData();
                gameSessionClient.checkAndGetHandler(CreateLobbyRes.class, ClientAPIEventType.LOBBYCREATED).handle(createLobbyRes);
            }
        }));
        //Outbound only PDU with 8B Long data and no action
        localPDUPipelines.put(PDUType.JOINLOBBYREQ, new LocalPDUPipeline().append(
                new PDUHandlerEncoder() {
            @Override
            public void encode(PDU in, Channel out) {
                JoinLobbyReq joinLobbyReq = (JoinLobbyReq) in.getData();
                in.setData(Unpooled.buffer(Long.BYTES).writeLong(joinLobbyReq.getLobbyID()));
                out.writeAndFlush(in);
            }
        }));
        //Inbound only PDU with 8B long data and action - a form of ack
        localPDUPipelines.put(PDUType.JOINLOBBYRES, new LocalPDUPipeline().append(
                new PDUHandlerDecoder() {
            @Override
            public void decode(PDU in, PDUInboundHandler out) {
                JoinLobbyRes joinLobbyRes = new JoinLobbyRes();
                joinLobbyRes.setLobbyID(((ByteBuf) in.getData()).readLong());
                in.setData(joinLobbyRes);
                out.handle(in);
            }}, new PDUInboundHandler(){

            @Override
            public void handle(PDU p) {
                JoinLobbyRes joinLobbyRes = (JoinLobbyRes) p.getData();
                gameSessionClient.checkAndGetHandler(JoinLobbyRes.class, ClientAPIEventType.LOBBYJOINED).handle(joinLobbyRes);
            }
        }));
        //Outbound PDU with no payload and no action (registered only)
        localPDUPipelines.put(PDUType.LEAVELOBBYREQ, new LocalPDUPipeline().append(
                (PDUHandlerEncoder) (p, c) -> {
                    p.setData(Unpooled.buffer(0));
                    c.writeAndFlush(p);
                }
        ));
        //Inbound PDU with no payload and action
        localPDUPipelines.put(PDUType.LEAVELOBBYRES, new LocalPDUPipeline().append(
                new PDUHandlerDecoder() {
            @Override
            public void decode(PDU in, PDUInboundHandler out) {
                LeaveLobbyRes leaveLobbyRes = new LeaveLobbyRes();
                leaveLobbyRes.setLeader(((ByteBuf) in.getData()).readBoolean());
                in.setData(leaveLobbyRes);
                out.handle(in);
            }}, new PDUInboundHandler(){

            @Override
            public void handle(PDU p) {
                LeaveLobbyRes leaveLobbyRes = (LeaveLobbyRes) p.getData();
                gameSessionClient.checkAndGetHandler(LeaveLobbyRes.class, ClientAPIEventType.MEMBERLEFT).handle(leaveLobbyRes);
            }
        }));
        //Inbound only 8B Long + 2 * 1B Byte
        localPDUPipelines.put(PDUType.LOBBYBEACON, new LocalPDUPipeline().append(
                new PDUHandlerDecoder() {
            @Override
            public void decode(PDU in, PDUInboundHandler out) {
                LobbyBeacon lobbyBeacon = new LobbyBeacon();
                ByteBuf byteBuf = (ByteBuf) in.getData();
                lobbyBeacon.setLobbyID(byteBuf.readLong());
                lobbyBeacon.setLobbyCurOccupancy(byteBuf.readByte());
                lobbyBeacon.setLobbyMaxOccupancy(byteBuf.readByte());
                lobbyBeacon.setLobbyListRefresh(byteBuf.readBoolean());
                in.setData(lobbyBeacon);
                out.handle(in);
            }}, new PDUInboundHandler() {

            @Override
            public void handle(PDU p) {
                LobbyBeacon lobbyBeacon = (LobbyBeacon) p.getData();
                gameSessionClient.checkAndGetHandler(LobbyBeacon.class, ClientAPIEventType.LOBBYBEACON).handle(lobbyBeacon);
            }
        }));
        //Non-empty variable length PDU (char array max size 64)
        localPDUPipelines.put(PDUType.CHATMESSAGE, new LocalPDUPipeline().append(
                new PDUStringEncoder(),
                new PDUStringEncoder(),
                (PDUInboundHandler) p -> {
                            gameSessionClient.checkAndGetHandler(ChatMessage.class, ClientAPIEventType.LOBBYCHATMESSAGERECEIVED).handle((ChatMessage) p.getData());
                }
        ));
    }
}
