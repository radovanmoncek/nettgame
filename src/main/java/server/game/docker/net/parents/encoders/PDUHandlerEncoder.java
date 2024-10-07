package server.game.docker.net.parents.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.parents.handlers.PDUHandler;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

/**
 * <p>
 *     The first step in the communication pipeline. This handler specifies, how (on high abstraction level) the data should be encoded for following transportation.
 * </p>
 */
public interface PDUHandlerEncoder extends PDUHandler {
    /**
     * <p>
     *     Specify an encoding pattern / mechanism used for transporting a POJO DTO class.
     * </p>
     * <p>
     *     "Meta-values" in {@link PDU} header like {@link Byte} iD of {@link PDUType} and {@link Integer}
     *     varLen will be automatically injected based on {@link PDUType} specification.
     *     The output {@link ByteBuf} should therefore contain only the desired payload of data that are to be transmitted.
     * </p>
     * <p>
     *     This operation should perform as a reverse mechanism of the decode method, if an identity transport of data is required.
     * </p>
     * <p>
     *     This is a <i>terminal operation</i>, results are passed as a {@link ByteBuf} and processed further as preparation to be sent to the wire.
     * </p>
     * <p>
     *     As its last step, this method is to write and flush encoded data to the {@link Channel}, else no communication can occur.
     * </p>
     * @param in {@link PDU} to transport
     */
    void encode(PDU in, Channel out);
}
