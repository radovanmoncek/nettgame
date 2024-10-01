package server.game.docker.net;

import io.netty.channel.Channel;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 *     Represents a container for individual handlers registered to it by the append method.
 *     Such handlers are executed via the ingest method and the sequence of their usage is automatically determined based on their nature.
 * </p>
 */
public class LocalPipeline {
    private final Vector<PDUHandler> handlers;

    public LocalPipeline() {
        handlers = new Vector<>();
    }

    public LocalPipeline append(PDUHandler... handlers) {
        this.handlers.addAll(List.of(handlers));
        return this;
    }

    public void ingest(PDU p){
        if(handlers.stream().noneMatch(h -> h instanceof PDUHandlerDecoder || h instanceof PDUInboundHandler))
            return;
        PDUHandlerDecoder decoder = handlers.stream().filter(h -> h instanceof PDUHandlerDecoder).map(h -> (PDUHandlerDecoder) h).findAny().orElse(null);
        PDUInboundHandler handler = handlers.stream().filter(h -> h instanceof PDUInboundHandler).map(h -> (PDUInboundHandler) h).findAny().orElse(null);
        if (decoder == null || handler == null)
            return;
        decoder.decode(p, handler);
    }

    public void ingest(PDU p, Channel ch){
        if(handlers.stream().noneMatch(h -> h instanceof PDUHandlerEncoder))
            return;
        PDUHandlerEncoder encoder = handlers.stream().filter(h -> h instanceof PDUHandlerEncoder).map(h -> (PDUHandlerEncoder) h).findAny().orElse(null);
        if(encoder == null)
            return;
        encoder.encode(p, ch);
    }

    /**
     * <p>
     *
     * </p>
     */
    @Deprecated
    public static class PDUHandlerLegacy {
        private final Map<PDUType, LocalPipeline> mappings;

        public PDUHandlerLegacy() {
            this.mappings = new HashMap<>();
        }

        /**
         * <p>
         *     This method appends a {@link LocalPipeline} to a specific {@link PDUType} to this {@link PDUHandlerLegacy}.
         *     This pipeline process is a high level abstraction of the underlying data transmission over the wire.
         * </p>
         * <p>
         *     Since {@link PDUHandlerLegacy} uses a {@link HashMap} internally, it is not possible to duplicate any existing entry by this method.
         *     Therefore any added {@link PDUType} handling is unique.
         * </p>
         * @param t
         * @param p encoder, decoder, and IoC (Inversion of Control) action, which then together form a pipeline process determined by the specific {@link LocalPipeline} implementation.
         * @return {@link PDUHandlerLegacy} for convenient chaining
         */
        public PDUHandlerLegacy appendPipeline(/*Byte packetID*/PDUType t, LocalPipeline p){
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

        public PDUHandlerLegacy appendPipeline(PDUType t){
    //        mappings.put(t, new PDUHandler());
            return this;
        }

        public void receive(PDU p){
    //        Object out = mappings.get(p.getPDUType()).decode((ByteBuf) p.getData());
    //        p.setData(out);
    //        mappings.get(p.getPDUType()).handle(p);
        }

        public void send(Channel c, PDU p){
    //        ByteBuf out = mappings.get(p.getPDUType()).encode(p.getData());
    //        p.setData(out);
    //        c.writeAndFlush(p);
        }
    }
}
