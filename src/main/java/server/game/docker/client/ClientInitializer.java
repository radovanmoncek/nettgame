package server.game.docker.client;

import io.netty.channel.Channel;
import server.game.docker.net.modules.beacons.decoders.PDULobbyBeaconHandlerDecoder;
import server.game.docker.net.modules.ids.decoders.PDUIDHandlerDecoder;
import server.game.docker.net.modules.requests.encoders.PDULobbyRequestEncoder;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.messages.decoders.PDUChatMessageDecoder;
import server.game.docker.net.modules.messages.encoders.PDUChatMessageEncoder;
import server.game.docker.net.modules.beacons.pdus.PDULobbyBeacon;
import server.game.docker.net.modules.updates.decoders.PDULobbyUpdateDecoder;
import server.game.docker.net.modules.updates.pdus.PDULobbyUpdate;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.routers.RouterHandler;

import java.util.Map;

public class ClientInitializer {
    private final GameSessionClient gameSessionClient;
    private final RouterHandler multiPipeline;

    public ClientInitializer(Channel clientChannel, Map<ClientAPIEventType, ClientAPIEventHandler<? extends PDU>> eventMappings, GameSessionClient gameSessionClient, RouterHandler multiPipeline) {
        this.gameSessionClient = gameSessionClient;
        this.multiPipeline = multiPipeline;
    }

    public void init() {
        multiPipeline.appendRead(PDUType.ID,
                        new PDUIDHandlerDecoder(),
                        new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameSessionClient.checkAndCallHandler(ClientAPIEventType.CONNECTED, in);
                            }
                        })
                .appendRead(PDUType.LOBBYREQUEST,
                        new PDULobbyRequestEncoder()
                )
                .appendRead(PDUType.LOBBYUPDATE,
                        new PDULobbyUpdateDecoder(),
                        new PDUInboundHandler() {
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
                .appendRead(PDUType.LOBBYBEACON,
                        new PDULobbyBeaconHandlerDecoder(),
                        new PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameSessionClient.checkAndCallHandler(ClientAPIEventType.LOBBYBEACON, in);
                            }
                        })
                .appendRead(PDUType.CHATMESSAGE,
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
