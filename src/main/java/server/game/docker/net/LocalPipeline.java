package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.util.List;
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
}
