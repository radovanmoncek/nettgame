package server.game.docker.net;

import io.netty.buffer.ByteBuf;
import server.game.docker.net.pdu.PDU;

import java.nio.ByteBuffer;

/**
 * <p>
 *
 * </p>
 */
public interface LocalPipeline {
    /**
     * <p>
     *     Specify a decoding pattern used for transporting a POJO DTO class.
     * </p>
     * <p>
     *     "Meta-values" in {@link PDU} header like {@link Byte} iD of {@link server.game.docker.net.pdu.PDUType} and {@link Integer} varLen will be automatically injected based on {@link server.game.docker.net.pdu.PDUType} specification.
     * </p>
     * <p>
     *     This operation should perform as a reverse mechanism of the encode method, if an identity transport of data is required.
     * </p>
     * <p>
     *     This is an <i>intermediate operation</i>, results are passed as a {@link PDU} into the terminal perform method for further processing.
     * </p>
     * @param in the {@link ByteBuf} passed and processed from the wire to be decoded
     * @return decoded POJO DTO class
     */
    Object decode(ByteBuf in);

    /**
     * <p>
     *     Specify an encoding pattern used for transporting a POJO DTO class.
     * </p>
     * <p>
     *     "Meta-values" in {@link PDU} header like {@link Byte} iD of {@link server.game.docker.net.pdu.PDUType} and {@link Integer} varLen will be automatically injected based on {@link server.game.docker.net.pdu.PDUType} specification.
     * </p>
     * <p>
     *     This operation should perform as a reverse mechanism of the decode method, if an identity transport of data is required.
     * </p>
     * <p>
     *     This is a <i>terminal operation</i>, results are passed as a {@link ByteBuf} and processed further as preparation to be sent to the wire.
     * </p>
     * @param in POJO DTO class to transport
     * @return a {@link ByteBuf} representation of the in POJO DTO class
     */
    ByteBuf encode(Object in);

    /**
     * <p>
     *     Specify an action taken after a {@link PDU} has been processed.
     * </p>
     * <p>
     *     This is a <i>terminal operation</i>, no further processing occurs after its call other than its own.
     * </p>
     * @param p the resulting constructed {@link PDU} after its transportation occurs and finishes
     */
    void perform(PDU p); //todo: generify to allow return of implementor defined POJO DTO - mapper will inject with decoded Object (generic?) //todo: GameDataPDU<T>
}
