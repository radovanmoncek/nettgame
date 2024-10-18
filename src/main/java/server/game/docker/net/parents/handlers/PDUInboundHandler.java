package server.game.docker.net.parents.handlers;

import io.netty.channel.Channel;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.enums.PDUType;

public abstract class PDUInboundHandler implements PDUHandler {
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
     * @param in the resulting constructed {@link PDU} after its transportation occurs and finishes
     */
    public abstract void handle(PDU in); //todo: generify to allow return of implementor defined POJO DTO - mapper will inject with decoded Object (generic?) //todo: GameDataPDU<T>

    public void handle(PDU in, Channel channel) {}
}
