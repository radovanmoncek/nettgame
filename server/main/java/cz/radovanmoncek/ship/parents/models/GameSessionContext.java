package cz.radovanmoncek.ship.parents.models;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Inspired by the Netty {@link io.netty.channel.ChannelHandlerContext}, this class serves as an object
 * representing the current relevant game session state passed between individual state changing events.
 */
public interface GameSessionContext {

    void performOnAllConnections(final Consumer<Channel> playerChannel);

    void broadcast(final Object message);

    void registerPlayerConnection(final Channel playerChannel);

    void unregisterPlayerConnection(final Channel playerChannel);
}
