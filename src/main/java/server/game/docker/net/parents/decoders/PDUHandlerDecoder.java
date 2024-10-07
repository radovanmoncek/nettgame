package server.game.docker.net.parents.decoders;

import io.netty.buffer.ByteBuf;
import server.game.docker.net.parents.handlers.PDUHandler;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.parents.handlers.PDUInboundHandler;
import server.game.docker.net.pdu.PDUType;

public interface PDUHandlerDecoder extends PDUHandler {
    /**
     * <p>
     *     Specify a decoding pattern / mechanism used for transporting a POJO DTO class.
     * </p>
     * <p>
     *     "Meta-values" in {@link PDU} header like {@link Byte} iD of {@link PDUType} and {@link Integer} varLen will be automatically injected based on {@link PDUType} specification.
     * </p>
     * <p>
     *     This operation should perform as a reverse mechanism of the encode method, if an identity transport of data is required.
     * </p>
     * <p>
     *     This is an <i>intermediate operation</i>, results are passed as a {@link PDU} into the terminal perform method for further processing.
     * </p>
     * @param in the {@link ByteBuf} passed and processed from the wire to be decoded
     */
    void decode(PDU in, PDUInboundHandler out);
}
