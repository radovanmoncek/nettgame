package server.game.docker.client;

import io.netty.channel.Channel;
import server.game.docker.GameServerInitializer;
import server.game.docker.client.modules.beacons.decoders.PDULobbyBeaconHandlerDecoder;
import server.game.docker.client.modules.ids.decoders.IDDecoder;
import server.game.docker.client.modules.requests.encoders.PDULobbyRequestEncoder;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.modules.messages.decoders.PDUChatMessageDecoder;
import server.game.docker.modules.messages.encoders.PDUChatMessageEncoder;
import server.game.docker.client.modules.updates.decoders.PDULobbyUpdateDecoder;
import server.game.docker.modules.updates.pdus.PDULobbyUpdate;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.Map;

public final class ClientInitializer {
    private final GameClient gameClient;
    private final GameServerInitializer.RouterHandler multiPipeline;

    public ClientInitializer(Channel clientChannel, Map<GameClient.ClientAPIEventType, GameClient.ClientAPIEventHandler<? extends PDU>> eventMappings, GameClient gameClient, GameServerInitializer.RouterHandler multiPipeline) {
        this.gameClient = gameClient;
        this.multiPipeline = multiPipeline;
    }

    public void init() {
        multiPipeline.appendRead(PDUType.ID,
                        new IDDecoder.PDUIDHandlerDecoder(),
                        new GameServerInitializer.PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.CONNECTED, in);
                            }
                        })
                .appendRead(PDUType.LOBBYREQUEST,
                        new PDULobbyRequestEncoder()
                )
                .appendRead(PDUType.LOBBYUPDATE,
                        new PDULobbyUpdateDecoder(),
                        new GameServerInitializer.PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                switch (((PDULobbyUpdate) in).getStateFlag()) {
                                    case 0 ->
                                            gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.LOBBYCREATED, in);
                                    case 1 -> gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.LOBBYJOINED, in);
                                    case 2 -> gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.LOBBYLEFT, in);
                                    case 3 -> gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.MEMBERJOINED, in);
                                    case 4 -> gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.MEMBERLEFT, in);
                                }
                            }
                        })
                .appendRead(PDUType.LOBBYBEACON,
                        new PDULobbyBeaconHandlerDecoder(),
                        new GameServerInitializer.PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                                gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.LOBBYBEACON, in);
                            }
                        })
                .appendRead(PDUType.CHATMESSAGE,
                        new PDUChatMessageEncoder(),
                        new PDUChatMessageDecoder(),
                        new GameServerInitializer.PDUInboundHandler() {
                            @Override
                            public void handle(PDU in) {
                            }

                            @Override
                            public void handle(PDU in, Channel channel) {
                                gameClient.checkAndCallHandler(GameClient.ClientAPIEventType.LOBBYCHATMESSAGERECEIVED, in);
                            }
                        }
                );
    }
}
