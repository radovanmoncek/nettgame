package container.game.docker.ship.examples.creators;

import container.game.docker.modules.examples.games.handlers.ExampleGameSessionHandler;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.products.Product;

public class SessionChannelGroupHandlerCreator extends Creator {

    @Override
    public Product newProduct() {

        return new ExampleGameSessionHandler();
    }
}
