package server.game.docker.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.PDUHandlerDecoder;
import server.game.docker.net.PDUInboundHandler;
import server.game.docker.net.PDUHandler;
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.dto.*;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.util.Map;

public class ClientInitializer {
    private final Channel clientChannel;
    private final Map<PDUType, LocalPipeline> localPDUPipelines;
    private final Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings;

    public ClientInitializer(Channel clientChannel, Map<PDUType, LocalPipeline> localPDUPipelines, Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings) {
        this.clientChannel = clientChannel;
        this.localPDUPipelines = localPDUPipelines;
        this.eventMappings = eventMappings;
    }

    public void init() {
        //Inbound only PDU with action
        localPDUPipelines.put(PDUType.IDRES, new PDUHandlerDecoder() {
            @Override
            public Object decode(ByteBuf in) {
                IDRes out = new IDRes();
                out.setNewClientID(in.readLong());
                return out;
            }

            @Override
            public void handle(PDU p) {
                checkAndGetHandler(IDRes.class, ClientAPIEventType.CONNECTED).handle((IDRes) p.getData());
            }
        });
        //Outbound PDU with no payload
        localPDUPipelines.put(PDUType.CREATELOBBYREQ, new PDUInboundHandler() {
            @Override
            public ByteBuf encode(Object in) {
                return Unpooled.buffer(0);
            }
        });
        //Inbound PDU with 32-bit Integer payload and required action
        localPDUPipelines.put(PDUType.CREATELOBBYRES, new PDUHandlerDecoder() {
            @Override
            public Object decode(ByteBuf in) {
                CreateLobbyRes out = new CreateLobbyRes();
                out.setLobbyId(in.readLong());
                return out;
            }

            @Override
            public void handle(PDU p) {
                CreateLobbyRes createLobbyRes = (CreateLobbyRes) p.getData();
                checkAndGetHandler(CreateLobbyRes.class, ClientAPIEventType.LOBBYCREATED).handle(createLobbyRes);
            }
        });
        //Outbound only PDU with 8B Long data and no action
        localPDUPipelines.put(PDUType.JOINLOBBYREQ, new PDUInboundHandler() {
            @Override
            public ByteBuf encode(Object in) {
                JoinLobbyReq joinLobbyReq = (JoinLobbyReq) in;
                return Unpooled.buffer(Long.BYTES).writeLong(joinLobbyReq.getLobbyID());
            }
        });
        //Inbound only PDU with 8B long data and action - a form of ack
        localPDUPipelines.put(PDUType.JOINLOBBYRES, new PDUHandlerDecoder() {
            @Override
            public Object decode(ByteBuf in) {
                JoinLobbyRes out = new JoinLobbyRes();
                out.setLobbyID(in.readLong());
                return out;
            }

            @Override
            public void handle(PDU p) {
                JoinLobbyRes joinLobbyRes = (JoinLobbyRes) p.getData();
                checkAndGetHandler(JoinLobbyRes.class, ClientAPIEventType.LOBBYJOINED).handle(joinLobbyRes);
            }
        });
        //Outbound PDU with no payload and no action (registered only)
        localPDUPipelines.put(PDUType.LEAVELOBBYREQ, new PDUHandler());
        //Inbound PDU with no payload and action
        localPDUPipelines.put(PDUType.LEAVELOBBYRES, new PDUHandlerDecoder() {
            @Override
            public Object decode(ByteBuf in) {
                LeaveLobbyRes out = new LeaveLobbyRes();
                out.setLeader(in.readBoolean());
                return out;
            }

            @Override
            public void handle(PDU p) {
                LeaveLobbyRes leaveLobbyRes = (LeaveLobbyRes) p.getData();
                checkAndGetHandler(LeaveLobbyRes.class, ClientAPIEventType.MEMBERLEFT).handle(leaveLobbyRes);
            }
        });
        //Inbound only 8B Long + 2 * 1B Byte
        localPDUPipelines.put(PDUType.LOBBYBEACON, new PDUHandlerDecoder() {
            @Override
            public Object decode(ByteBuf in) {
                LobbyBeacon out = new LobbyBeacon();
                out.setLobbyID(in.readLong());
                out.setLobbyCurOccupancy(in.readByte());
                out.setLobbyMaxOccupancy(in.readByte());
                out.setLobbyListRefresh(in.readBoolean());
                return out;
            }

            @Override
            public void handle(PDU p) {
                LobbyBeacon lobbyBeacon = (LobbyBeacon) p.getData();
                checkAndGetHandler(LobbyBeacon.class, ClientAPIEventType.LOBBYBEACON).handle(lobbyBeacon);
            }
        });
    }

    private <T> ClientAPIEventHandler<T> checkAndGetHandler(Class<T> c, ClientAPIEventType eventType) {
        ClientAPIEventHandler<?> h = eventMappings.get(eventType);
//        if(((ParameterizedType) h.getClass().getGenericSuperclass()).getActualTypeArguments()[0].equals(((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0]))
        return (ClientAPIEventHandler<T>) h;
//        return null;
    }
}
