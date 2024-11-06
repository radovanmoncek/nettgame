package server.game.docker;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.modules.ids.IDServerFacade;
import server.game.docker.modules.ids.encoders.IDEncoder;
import server.game.docker.modules.ids.handlers.ServerIDHandler;
import server.game.docker.modules.requests.LobbyRequestServerFacade;
import server.game.docker.modules.requests.decoder.LobbyRequestDecoder;
import server.game.docker.modules.requests.handlers.ServerLobbyRequestHandler;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public final class GameServerInitializer extends ChannelInitializer<SocketChannel>{
    private final IDServerFacade iDServerFacade;
    private final LobbyRequestServerFacade lobbyRequestServerFacade;

    public GameServerInitializer(final IDServerFacade iDServerFacade, final LobbyRequestServerFacade lobbyRequestServerFacade) {
        this.iDServerFacade = iDServerFacade;
        this.lobbyRequestServerFacade = lobbyRequestServerFacade;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new LobbyRequestDecoder(),
                new ServerIDHandler(iDServerFacade),
                new ServerLobbyRequestHandler(lobbyRequestServerFacade),
                new IDEncoder()
        );
    }

    /**
     * <p>
     *     Represents a container for individual handlers registered to it via the append method.
     *     Such handlers are executed through the ingest method and the sequence of their usage is automatically determined based on their function by the ingest method.
     * </p>
     */
    public static final class RouterHandler implements PDURouter {
        private final Map<PDUType, Vector<PDUHandler>> handlers;

        public RouterHandler() {
            handlers = new HashMap<>();
        }

        /**
         * Appends a {@link RouterHandler} to a pipeline stack associated with the supplied {@link PDUType}.
         * @param type the {@link PDUType} of the associated pipeline stack
         * @param handler the {@link RouterHandler} to append
         * @return This {@link RouterHandler} for convenient chaining
         */
        public RouterHandler appendRead(PDUType type, PDUHandler handler) {
            handlers.computeIfAbsent(type, k -> new Vector<>()).add(handler);
            return this;
        }

        @Deprecated
        public RouterHandler appendRead(PDUType type, PDUHandler... handlers) {
            this.handlers.computeIfAbsent(type, k -> new Vector<>()).addAll(List.of(handlers));
            return this;
        }

        /**
         * This is the pipeline outbound entry point, the PDU given is processed accordingly and sent to the appropriate {@link PDUHandler}.
         * @param in {@link PDU} the PDU to process
         */
        public void route(PDUType type, ByteBuf in, Channel channel){
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
        public void route(PDUType type, PDU in, Channel channel){
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

    /**
     * Tagging interface for classes representing a PDU handler.
     */
    @Deprecated
    public static interface PDUHandler {
    }

    public static interface PDUHandlerDecoder extends PDUHandler {
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
        void decode(ByteBuf in, Channel channel, PDUInboundHandler out);
    }

    /**
     * <p>
     *     The first step in the communication pipeline. This handler specifies, how (on high abstraction level) the data should be encoded for following transportation.
     * </p>
     */
    public static interface PDUHandlerEncoder extends PDUHandler {
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

    public abstract static class PDUInboundHandler implements PDUHandler {
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

    public static interface PDURouter {
    }
}
