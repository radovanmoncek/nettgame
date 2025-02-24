package container.game.docker.ship.parents.models;

import container.game.docker.ship.parents.products.Product;
import io.netty.channel.ChannelId;

import java.util.Optional;

public interface PlayerSessionData extends Product {

    Optional<ChannelId> retrievePlayerChannelId();

    void placePlayerChannelId(final ChannelId channelId);
}
