package container.game.docker.ship.examples;

import io.netty.channel.ChannelId;
import container.game.docker.modules.session.handlers.SessionChannelGroupHandler;
import container.game.docker.ship.bootstrap.InstanceContainer;

public final class SampleInstanceContainer {
    public static void main(String[] args) throws Exception {
            InstanceContainer
                    .newInstance()
                    .withChannelGroupHandlerSupplier(SessionChannelGroupHandler::new)
                        private static final Integer X_BOUND = 800, Y_BOUND = 600;
                        private Long tickCounter = 0L;
                        private ChannelId player1, player2;
                        private Integer x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                    .run(4);
    }
}
