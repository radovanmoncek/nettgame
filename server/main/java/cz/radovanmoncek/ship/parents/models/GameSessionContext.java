package cz.radovanmoncek.ship.parents.models;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Inspired by the Netty {@link io.netty.channel.ChannelHandlerContext}, this class serves as an object
 * representing the current relevant game session state passed between individual state changing events.
 * <p>
 *     This class contains several crucial utilities for manipulating connections.
 * </p>
 * @author Radovan Monček
 * @since 1.0
 */
public interface GameSessionContext {

    /**
     * Performs a {@link Consumer#accept(Object)} call for each currently registered connection.
     * @param action the action to perform.
     */
    void performOnAllConnections(final Consumer<Channel> action);

    /**
     * Broadcast a message to all currently registered connections.
     * @apiNote This is a utility method aiming to replace the construct:
     *     <code>
     * context.performOnAllConnections(playerChannel -> {playerChannel.writeAndFlush(message);});
     *     </code>
     * @param message the message Object to broadcast.
     */
    void broadcast(final Object message);

    /**
     * Register a channel to this game session.
     * @param playerChannel the {@link Channel} to register.
     */
    void registerPlayerConnection(final Channel playerChannel);

    /**
     * Unregister a {@link Channel} from this game session.
     * @param playerChannel the {@link Channel} to unregister.
     */
    void unregisterPlayerConnection(final Channel playerChannel);

    /**
     * Performs a {@link BiConsumer#accept(Object, Object)} call for each connected channel with information about the current iteration number.
     * Please note that, since the connections are backed by a {@link Set} structure, the index number is purely orientational,
     * and does not (and can not) represent connection ordering.
     * @apiNote This is a utility alternative to {@link GameSessionContext#performOnAllConnections(Consumer)};
     * if only an iteration over all current connections is desired, these two methods may be used interchangeably.
     * @param action the action to perform.
     */
    void performOnAllConnectionsWithIndex(final BiConsumer<Channel, Integer> action);
}
