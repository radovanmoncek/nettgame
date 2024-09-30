package server.game.docker.net;

import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

public interface PDUInboundHandler extends PDUHandler {
    /**
     * <p>
     *     A handler that is used to perform an action as a result of receiving a given {@link PDUType} {@link PDU}.
     * </p>
     * <p>
     *     Specify an action taken after a {@link PDU} has been processed.
     * </p>
     * <p>
     *     This is a <i>terminal operation</i>, no further processing occurs after its call other than its own.
     * </p>
     * @param p the resulting constructed {@link PDU} after its transportation occurs and finishes
     */
    void handle(PDU p); //todo: generify to allow return of implementor defined POJO DTO - mapper will inject with decoded Object (generic?) //todo: GameDataPDU<T>
}
