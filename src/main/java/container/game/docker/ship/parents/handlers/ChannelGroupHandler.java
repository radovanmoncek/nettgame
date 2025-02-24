package container.game.docker.ship.parents.handlers;

import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.models.PlayerSessionData;
import container.game.docker.ship.parents.products.Product;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class ChannelGroupHandler<P1 extends ProtocolDataUnit, P2 extends ProtocolDataUnit> extends SimpleChannelInboundHandler<P1> implements Product {
    private static final Logger logger = LogManager.getLogger(ChannelGroupHandler.class);
    private Creator playerSessionDataCreator;
    /**
     * All the client channels connected to this server
     */
    private static final ChannelGroup players;
    /**
     * Data session kept for every player during the duration of their connection to the InstanceContainer.
     * Each player specific session is shared withing, and between each, and all registered handlers.
     */
    private static final ConcurrentHashMap<ChannelId, PlayerSessionData> playerSessions;

    static {

        players = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        playerSessions = new ConcurrentHashMap<>();
    }

    @Override
    protected final void channelRead0(final ChannelHandlerContext channelHandlerContext, final P1 protocolDataUnit) {

        playerChannelRead(protocolDataUnit, playerSessions.get(channelHandlerContext.channel().id()));
    }

    abstract protected void playerChannelRead(final P1 protocolDataUnit, final PlayerSessionData playerSession);

    @Override
    public final void handlerAdded(final ChannelHandlerContext channelHandlerContext) {

        if(!players.add(channelHandlerContext.channel()))
            return;

        final var playerSessionData = (PlayerSessionData) playerSessionDataCreator.newProduct();

        playerSessionData.placePlayerChannelId(channelHandlerContext.channel().id());

        playerSessions.put(channelHandlerContext.channel().id(), playerSessionData);

        logger.info("Player with ChannelId {} has connected", channelHandlerContext.channel().id());
    }

    @Override
    public final void handlerRemoved(final ChannelHandlerContext channelHandlerContext) {

        if(!players.remove(channelHandlerContext.channel()))
            return;

        playerSessions.remove(channelHandlerContext.channel().id());
    }

    @Override
    public final void channelUnregistered(final ChannelHandlerContext channelHandlerContext) {

        playerDisconnected(playerSessions.get(channelHandlerContext.channel().id()));

        logger.info("Player with ChannelId {} has disconnected", channelHandlerContext.channel().id());
    }

    @Override
    public final void channelRegistered(final ChannelHandlerContext channelHandlerContext) {

        logger.info("Player Channel with ChannelId {} has registered", channelHandlerContext.channel().id());
    }

    abstract protected void playerDisconnected(final PlayerSessionData playerSession);

    protected final void unicastToClientChannel(final P2 protocolDataUnit, final ChannelId channelId) {

        unicastToClientChannel(protocolDataUnit, players.find(channelId));
    }

    protected final void unicastToClientChannel(final P2 protocolDataUnit, final Channel clientChannel){

        if (clientChannel == null) {

            logger.error("Client channel is null");

            return;
        }

        players.find(clientChannel.id()).writeAndFlush(protocolDataUnit);
    }

    protected final void multicastToClientChannels(final P2 protocolDataUnit, final Channel ... clientChannels){

        List.of(clientChannels).forEach(channel -> unicastToClientChannel(protocolDataUnit, channel));
    }

    protected final void multicastToClientChannelIds(final P2 protocolDataUnit, final ChannelId... clientChannelIds){

        multicastToClientChannels(protocolDataUnit, Stream.of(clientChannelIds).map(players::find).toArray(Channel[]::new));
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause) {

        logger.error(cause.getMessage(), cause);
    }
}
