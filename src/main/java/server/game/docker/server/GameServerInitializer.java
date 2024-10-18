package server.game.docker.server;

import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.beacons.encoders.PDULobbyBeaconHandlerEncoder;
import server.game.docker.net.modules.ids.decoders.PDUIDHandlerDecoder;
import server.game.docker.net.modules.ids.encoders.PDUIDHandlerEncoder;
import server.game.docker.net.modules.messages.decoders.PDUChatMessageDecoder;
import server.game.docker.net.modules.messages.encoders.PDUChatMessageEncoder;
import server.game.docker.net.modules.requests.decoder.PDULobbyRequestDecoder;
import server.game.docker.net.modules.updates.encoders.PDULobbyUpdateEncoder;
import server.game.docker.net.parents.handlers.PDUHandler;
import server.game.docker.net.routers.RouterHandler;
import server.game.docker.server.net.handlers.PDUChatInboundHandler;

public class GameServerInitializer {
    private final RouterHandler multiPipeline;
    private final GameServer gameServer;
    private final PDUHandler lobbyPDUInboundHandler;

    public GameServerInitializer(
            RouterHandler multiPipeline,
            GameServer gameServer, PDUHandler lobbyPDUInboundHandler
    ) {
        this.multiPipeline = multiPipeline;
        this.gameServer = gameServer;
        this.lobbyPDUInboundHandler = lobbyPDUInboundHandler;
    }

    public void init() {
        /*--------ID - handshake with server--------*/
        //Outbound only PDU containing 64-bit Long clientID
        multiPipeline.appendRead(PDUType.ID,
                        new PDUIDHandlerEncoder()
                )
                /*--------LOBBY--------*/
                //Inbound PDU no payload and action
                .appendRead(PDUType.LOBBYREQUEST,
                        new PDULobbyRequestDecoder(),
                        lobbyPDUInboundHandler
                )
                .appendRead(PDUType.LOBBYUPDATE,
                        new PDULobbyUpdateEncoder()
                )
                //Outbound only PDU with no action, 8B Long, 2 * 1B Byte data and 1B Byte - Boolean Lobby list refresh flag
                .appendRead(PDUType.LOBBYBEACON,
                        new PDULobbyBeaconHandlerEncoder()
                )
                .appendRead(PDUType.CHATMESSAGE,
                        new PDUChatMessageEncoder(),
                        new PDUChatMessageDecoder(),
                        new PDUChatInboundHandler(gameServer)
                );
    }
}
