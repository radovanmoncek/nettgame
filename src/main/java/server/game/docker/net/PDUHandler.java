package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.client.GameClient;
import server.game.docker.net.pdu.PDU;
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

    /**
     * <p>
     *     This method enables the registration of a {@link PDUType} to this {@link GameClient} along with its encoder, decoder, and IoC (Inversion of Control) action,
     *     which then together form a pipeline process determined by the specific {@link LocalPipeline} implementation.
     *     This pipeline process is a high level abstraction of the underlying data transmission over the wire.
     * </p>
     * <p>
     *     Since {@link PDUHandler} uses a {@link HashMap} internally, it is not possible to duplicate any existing entry by this method.
     *     Therefore any added {@link PDUType} handling is unique.
     * </p>
     * @param t
     * @param p
     * @return {@link PDUHandler} for convenient chaining
     */
    public PDUHandler registerPDU(/*Byte packetID*/PDUType t, LocalPipeline p){
        mappings.put(/*packetID*/t, p);
        return this;
    }

    @Deprecated
    public LocalPipeline map(/*DatagramPacket packetToMap*/PDUType twoByteHeader){
//        GameDataPDU routedPacket = new GameDataPDU(Byte.parseByte(new String(packetToMap.getData()).substring(0, 2)), packetToMap.getData());
//        routedPacket.setAddress(packetToMap.getAddress());
//        routedPacket.setPort(packetToMap.getPort());
//        packetActionMappings.get(Byte.parseByte(new String(packetToMap.getData()).substring(0, 2))).perform(routedPacket);
        return mappings.get(twoByteHeader);
    }

    public PDUHandler registerPDU(PDUType t){
        mappings.put(t, new DefaultLocalPipeline());
        return this;
    }

    public void receive(PDU p){
        Object out = mappings.get(p.getGameDataPDUType()).decode(p.getByteBuf());
        p.setData(out);
        mappings.get(p.getGameDataPDUType()).perform(p);
    }

    public void send(Channel c, PDU p){
        ByteBuf out = mappings.get(p.getGameDataPDUType()).encode(p.getData());
        p.setByteBuf(out);
        c.writeAndFlush(p);
    }
}
