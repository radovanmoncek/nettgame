package server.game.docker.client;

import server.game.docker.net.LocalPipeline;
import server.game.docker.net.pdu.PDUType;

import java.util.Map;

public class ClientAPIInitializer {
    public ClientAPIInitializer(Map<PDUType, LocalPipeline> localPipelines, Map<ClientAPIEventType, Object> eventMappings) {
    }
}
