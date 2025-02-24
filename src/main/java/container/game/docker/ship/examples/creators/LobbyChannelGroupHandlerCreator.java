package container.game.docker.ship.examples.creators;

import container.game.docker.modules.examples.lobbies.handlers.LobbyChannelGroupHandler;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.products.Product;

public class LobbyChannelGroupHandlerCreator extends Creator {

    @Override
    public Product newProduct() {

        return new LobbyChannelGroupHandler();
    }
}
