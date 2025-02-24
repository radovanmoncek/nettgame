package container.game.docker.ship.examples.creators;

import container.game.docker.modules.examples.lobbies.codecs.LobbyResponseEncoder;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.products.Product;

public class LobbyResponseEncoderCreator extends Creator {

    @Override
    public Product newProduct() {

        return new LobbyResponseEncoder();
    }
}
