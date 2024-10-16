package server.game.docker.net.pipelines;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.parents.handlers.PDUHandler;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.parents.pdus.PDU;

import java.util.*;

/**
 * <p>
 *     Represents a container for individual handlers registered to it via the append method.
 *     Such handlers are executed through the ingest method and the sequence of their usage is automatically determined based on their function by the ingest method.
 * </p>
 */
public final class PDUMultiPipeline {
    private final Map<PDUType, Vector<PDUHandler>> handlers;

    public PDUMultiPipeline() {
        handlers = new HashMap<>();
    }

    /**
     * Appends a {@link PDUHandler} to a pipeline stack associated with the supplied {@link PDUType}.
     * @param type the {@link PDUType} of the associated pipeline stack
     * @param handler the {@link PDUHandler} to append
     * @return This {@link PDUMultiPipeline} for convenient chaining
     */
    public PDUMultiPipeline append(PDUType type, PDUHandler handler) {
        handlers.computeIfAbsent(type, k -> new Vector<>()).add(handler);
        return this;
    }

    public PDUMultiPipeline append(PDUType type, PDUHandler... handlers) {
        this.handlers.computeIfAbsent(type, k -> new Vector<>()).addAll(List.of(handlers));
        return this;
    }

    /**
     * This is the pipeline outbound entry point, the PDU given is processed accordingly and sent to the appropriate {@link PDUHandler}.
     * @param in {@link PDU} the PDU to process
     */
    public void ingest(PDUType type, ByteBuf in, Channel channel){
        assert in != null : new NullPointerException("PDU is null");
        if(handlers.get(type).stream().noneMatch(h -> h instanceof PDUHandlerDecoder || h instanceof PDUInboundHandler))
            return;
        PDUHandlerDecoder decoder = handlers.get(type).stream().filter(h -> h instanceof PDUHandlerDecoder).map(h -> (PDUHandlerDecoder) h).findAny().orElse(null);
        PDUInboundHandler handler = handlers.get(type).stream().filter(h -> h instanceof PDUInboundHandler).map(h -> (PDUInboundHandler) h).findAny().orElse(null);
        if (decoder == null && handler != null) {
            handler.handle(new PDU() {});
            return;
        }
        if(decoder == null || handler == null)
            return;
        decoder.decode(in, channel, handler);
    }

    /**
     * This the pipeline inbound entry point.
     * Given PDU is encoded using an appropriate previously registered {@link PDUHandlerEncoder}, and any further processing is delegated to it.
     * @param type the {@link PDUType} of the given {@link PDU}
     * @param in the {@link PDU} to process
     * @param channel the context {@link Channel}
     */
    public void ingest(PDUType type, PDU in, Channel channel){
        assert in != null : new NullPointerException("PDU cannot be null");
        assert channel != null : new NullPointerException("Channel must not be null");
        if(handlers.get(type).stream().noneMatch(h -> h instanceof PDUHandlerEncoder)) {
            return;
        }
        PDUHandlerEncoder encoder = handlers.get(type).stream().filter(h -> h instanceof PDUHandlerEncoder).map(h -> (PDUHandlerEncoder) h).findAny().orElse(null);
        if(encoder == null)
            return;
        encoder.encode(in, channel);
    }
}
