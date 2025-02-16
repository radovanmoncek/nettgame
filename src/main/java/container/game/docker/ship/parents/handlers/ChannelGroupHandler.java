package container.game.docker.ship.parents.handlers;

import container.game.docker.ship.data.structures.MultiValueTypeMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class ChannelGroupHandler<P1 extends ProtocolDataUnit, P2 extends ProtocolDataUnit> extends SimpleChannelInboundHandler<P1> {
    /**
     * All the client channels connected to this server
     */
    private static final ChannelGroup players;
    private static final ConcurrentHashMap<ChannelId, MultiValueTypeMap> playerSessions;

    static {

        players = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        playerSessions = new ConcurrentHashMap<>();
    }

    @Override
    protected final void channelRead0(final ChannelHandlerContext channelHandlerContext, final P1 protocolDataUnit) {

        playerChannelRead(protocolDataUnit, playerSessions.get(channelHandlerContext.channel().id()));
    }

    abstract protected void playerChannelRead(final P1 protocolDataUnit, final MultiValueTypeMap playerSession);

    @Override
    public final void handlerAdded(final ChannelHandlerContext channelHandlerContext) {

        if(!players.add(channelHandlerContext.channel()))
            return;

        playerSessions.put(channelHandlerContext.channel().id(), MultiValueTypeMap.of("playerChannelId", channelHandlerContext.channel().id()));

        System.out.printf("Player with ChannelId %s has connected\n", channelHandlerContext.channel().id()); //todo: log4j
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

        System.out.printf("Player with ChannelId %s has disconnected\n", channelHandlerContext.channel().id()); //todo: log4j
    }

    @Override
    public final void channelRegistered(final ChannelHandlerContext channelHandlerContext) {

//        todo:
    }

    abstract protected void playerDisconnected(final MultiValueTypeMap playerSession);

    protected final void unicastToClientChannel(final P2 protocolDataUnit, final ChannelId channelId) {

        unicastToClientChannel(protocolDataUnit, players.find(channelId));
    }

    protected final void unicastToClientChannel(final P2 protocolDataUnit, final Channel clientChannel){

        if (clientChannel == null) {

            System.err.println("Client channel is null"); //todo: log4j

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

        cause.printStackTrace(); //todo: log4j
    }
}
