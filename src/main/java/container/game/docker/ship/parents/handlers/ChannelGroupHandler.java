package container.game.docker.ship.parents.handlers;

import container.game.docker.modules.examples.session.models.SessionFlag;
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
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public abstract class ChannelGroupHandler<P1 extends ProtocolDataUnit, P2 extends ProtocolDataUnit, S> extends SimpleChannelInboundHandler<P1> {
    /**
     * All the client channels connected to this server
     */
    private static final ChannelGroup players;
    private static final ThreadGroup gameSessions;
    private S playerSession;

    static {

        players = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        gameSessions = new ThreadGroup("Managed Sessions");
    }

    @Override
    protected final void channelRead0(final ChannelHandlerContext channelHandlerContext, final P1 protocolDataUnit) {

        playerChannelRead(protocolDataUnit, channelHandlerContext.channel().id());
    }

    abstract protected void playerChannelRead(final P1 protocolDataUnit, final ChannelId playerChannelId);

    @Override
    public final void handlerAdded(final ChannelHandlerContext channelHandlerContext) {

        if(players.contains(channelHandlerContext.channel()))
            return;

        players.add(channelHandlerContext.channel());

        System.out.printf("A client has connected with channel id(): %s\n", channelHandlerContext.channel().id()); //todo: log4j
    }

    @Override
    public final void handlerRemoved(final ChannelHandlerContext channelHandlerContext) {

        playerDisconnected(channelHandlerContext.channel().id());

        players.remove(channelHandlerContext.channel());

        System.out.printf("Client with ChannelId %s has disconnected\n", channelHandlerContext.channel().id()); //todo: log4j
    }

    abstract protected void playerDisconnected(final ChannelId id);

    protected final void unicastToClientChannel(final P2 protocolDataUnit, final ChannelId channelId) {

        unicastToClientChannel(protocolDataUnit, players.find(channelId));
    }

    protected final void startManagingSession(final BiFunction<Integer, SessionFlag, Boolean> endCondition){

        final var runnable = new Runnable(){

            @Override
            public void run() {

                endCondition.apply(this.hashCode(), SessionFlag.START);

                while(endCondition.apply(this.hashCode(), SessionFlag.STATE)){

                    try {

                        TimeUnit.MILLISECONDS.sleep(33);
                    }
                    catch (final InterruptedException interruptedException){

                        interruptedException.printStackTrace(); //todo: log4j
                    }
                }

                endCondition.apply(this.hashCode(), SessionFlag.STOP);

                System.out.printf("Session %d has ended\n", hashCode()); //todo: log4j
            }
        };

        final var sessionThread = new Thread(gameSessions, runnable);

        sessionThread.setName(String.format("Game Session Thread %s", sessionThread.hashCode()));

        sessionThread.start();
    }

    protected final void unicastToClientChannel(final P2 protocolDataUnit, final Channel clientChannel){
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

        System.err.println(cause.getMessage()); //todo: log4j
    }
}
