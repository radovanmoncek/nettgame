package container.game.docker.ship.examples.creators;

import container.game.docker.ship.examples.models.ExampleNetworkedGamePlayerSessionData;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.products.Product;

public class PlayerSessionDataCreator extends Creator {

    @Override
    public Product newProduct() {

        return new ExampleNetworkedGamePlayerSessionData();
    }
}
