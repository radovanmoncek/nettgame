package cz.radovanmoncek.nettgame.nettgame.ship.bay.events;

import cz.radovanmoncek.nettgame.nettgame.ship.bay.models.GameSessionConfigurationOption;
import cz.radovanmoncek.nettgame.nettgame.ship.deck.events.GameSessionEventListener;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class GameSessionEventLoopTest {

    private static final AtomicBoolean tickOccurred = new AtomicBoolean(false);

    private static GameSessionEventLoop eventLoop;

    @BeforeAll
    static void setup() {

        final var tempChannel = new EmbeddedChannel();

        eventLoop = new GameSessionEventLoop(
                new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
                new GameSessionEventListener() {

                    @Override
                    public void onErrorThrown(GameSessionContext context, Throwable throwable) {

                    }

                    @Override
                    public void onInitialize(GameSessionContext context) {

                        context.registerPlayerConnection(tempChannel);
                    }

                    @Override
                    public void onStart(GameSessionContext context) {

                    }

                    @Override
                    public void onServerTick(GameSessionContext context) {

                        tickOccurred.set(true);

                        context.unregisterPlayerConnection(tempChannel);
                    }

                    @Override
                    public void onEnded(GameSessionContext context) {

                    }

                    @Override
                    public void onContextConnectionsEmpty(GameSessionContext context) {

                    }

                    @Override
                    public void onContextConnection(GameSessionContext context, Channel channel) {

                    }

                    @Override
                    public void onContextConnectionClosed(GameSessionContext context) {

                    }

                    @Override
                    public void onContextConnectionCountChanged(GameSessionContext context) {

                    }

                    @Override
                    public void onGlobalConnectionEvent(GameSessionContext context, Channel playerChannel) {

                    }
                },
                new LinkedList<>(List.of(new AbstractMap.SimpleEntry<>(GameSessionConfigurationOption.MAX_PLAYERS, 1))));
    }

    @Test
    void runTest() throws InterruptedException {

        try (final var executor = Executors.newSingleThreadExecutor()) {

            executor.submit(eventLoop);

            TimeUnit.MILLISECONDS.sleep(80);

            assertTrue(tickOccurred.get());
        }
    }
}
