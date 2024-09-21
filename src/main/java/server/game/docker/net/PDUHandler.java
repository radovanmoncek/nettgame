package server.game.docker.net;

import server.game.docker.net.pdu.PDUType;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 */
public class PDUHandler {
    private final Map<PDUType, LocalPipeline> mappings;

    public PDUHandler() {
        this.mappings = new HashMap<>();
    }

    public PDUHandler withMapping(/*Byte packetID*/PDUType twoByteHeader, LocalPipeline packetAction){
        mappings.put(/*packetID*/twoByteHeader, packetAction);
        return this;
    }

    public LocalPipeline map(/*DatagramPacket packetToMap*/PDUType twoByteHeader){
//        GameDataPDU routedPacket = new GameDataPDU(Byte.parseByte(new String(packetToMap.getData()).substring(0, 2)), packetToMap.getData());
//        routedPacket.setAddress(packetToMap.getAddress());
//        routedPacket.setPort(packetToMap.getPort());
//        packetActionMappings.get(Byte.parseByte(new String(packetToMap.getData()).substring(0, 2))).perform(routedPacket);
        return mappings.get(twoByteHeader);
    }
}
