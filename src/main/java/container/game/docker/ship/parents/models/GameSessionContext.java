package container.game.docker.ship.parents.models;

import container.game.docker.ship.parents.products.Product;
import container.game.docker.ship.models.GameSessionConfiguration;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameSessionContext extends Product {

    Optional<UUID> retrieveGameSessionUUID();

    List<Channel> retrievePlayerChannels();

    void broadcast(final Object message);
}
