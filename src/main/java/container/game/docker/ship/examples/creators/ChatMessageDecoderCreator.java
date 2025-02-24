package container.game.docker.ship.examples.creators;

import container.game.docker.modules.examples.chats.codecs.ChatMessageDecoder;
import container.game.docker.ship.parents.creators.Creator;
import container.game.docker.ship.parents.products.Product;

public class ChatMessageDecoderCreator extends Creator {

    @Override
    public Product newProduct() {

        return new ChatMessageDecoder();
    }
}
