package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.handlers.PDUHandler;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.pdu.*;

import java.util.*;

/**
 * <p>
 *     Represents a container for individual handlers registered to it by the append method.
 *     Such handlers are executed via the ingest method and the sequence of their usage is automatically determined based on their nature by the ingest method.
 * </p>
 */
public final class LocalPDUPipeline {
    private final Vector<PDUHandler> handlers;

    public LocalPDUPipeline() {
        handlers = new Vector<>();
    }

    public LocalPDUPipeline append(PDUHandler handler) {
        handlers.add(handler);
        return this;
    }

    public LocalPDUPipeline append(PDUHandler... handlers) {
        this.handlers.addAll(List.of(handlers));
        return this;
    }

    /**
     * This is the pipeline entry point, the PDU given is processed accordingly and sent to the appropriate {@link PDUHandler}.
     * @param p {@link PDU} the PDU to process
     */
    public void ingest(PDU p){
        if(p == null)
            throw new NullPointerException("PDU cannot be null");
        if(Objects.isNull(p.getData()) || !(p.getData() instanceof ByteBuf))
            throw new IllegalArgumentException("PDU data must not be null nor not an instance of ByteBuff");
        if(handlers.stream().noneMatch(h -> h instanceof PDUHandlerDecoder || h instanceof PDUInboundHandler))
            return;
        PDUHandlerDecoder decoder = handlers.stream().filter(h -> h instanceof PDUHandlerDecoder).map(h -> (PDUHandlerDecoder) h).findAny().orElse(null);
        PDUInboundHandler handler = handlers.stream().filter(h -> h instanceof PDUInboundHandler).map(h -> (PDUInboundHandler) h).findAny().orElse(null);
        if (decoder == null && handler != null) {
            handler.handle(p);
            return;
        }
        if(decoder == null || handler == null)
            return;
        decoder.decode(p, handler);
    }

    public void ingest(PDU p, Channel ch){
        if(ch == null)
            throw new NullPointerException("Channel must not be null");
        final PDU pFinal = new PDU();
        pFinal.setData(p.getData());
        pFinal.setAddress(p.getAddress());
        pFinal.setPDUType(p.getPDUType());
        if(Objects.isNull(p.getData()) || (pFinal.getData() instanceof ByteBuf)) {
            throw new IllegalArgumentException("PDU data must not be null or an instance of ByteBuf");
        }
        if(handlers.stream().noneMatch(h -> h instanceof PDUHandlerEncoder)) {
            return;
        }
        PDUHandlerEncoder encoder = handlers.stream().filter(h -> h instanceof PDUHandlerEncoder).map(h -> (PDUHandlerEncoder) h).findAny().orElse(null);
        if(encoder == null)
            return;
        encoder.encode(pFinal, ch);
    }

    /**
     * <p>
     *
     * </p>
     */
    @Deprecated
    public static class PDUHandlerLegacy {
        private final Map<PDUType, LocalPDUPipeline> mappings;

        public PDUHandlerLegacy() {
            this.mappings = new HashMap<>();
        }

        /**
         * <p>
         *     This method appends a {@link LocalPDUPipeline} to a specific {@link PDUType} to this {@link PDUHandlerLegacy}.
         *     This pipeline process is a high level abstraction of the underlying data transmission over the wire.
         * </p>
         * <p>
         *     Since {@link PDUHandlerLegacy} uses a {@link HashMap} internally, it is not possible to duplicate any existing entry by this method.
         *     Therefore any added {@link PDUType} handling is unique.
         * </p>
         * @param t
         * @param p encoder, decoder, and IoC (Inversion of Control) action, which then together form a pipeline process determined by the specific {@link LocalPDUPipeline} implementation.
         * @return {@link PDUHandlerLegacy} for convenient chaining
         */
        public PDUHandlerLegacy appendPipeline(/*Byte packetID*/PDUType t, LocalPDUPipeline p){
            mappings.put(/*packetID*/t, p);
            return this;
        }

        @Deprecated
        public LocalPDUPipeline map(/*DatagramPacket packetToMap*/PDUType twoByteHeader){
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
