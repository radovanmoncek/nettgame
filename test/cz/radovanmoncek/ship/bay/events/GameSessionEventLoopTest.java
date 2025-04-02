package cz.radovanmoncek.ship.bay.events;

import cz.radovanmoncek.ship.bay.models.GameSessionConfigurationOption;
import cz.radovanmoncek.ship.deck.events.GameSessionContext;
import cz.radovanmoncek.ship.deck.events.GameSessionEventListener;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class GameSessionEventLoopTest {
    private static GameSessionEventLoop eventLoop;
    private static GameSessionEventListener eventListener;
    private static ConcurrentLinkedQueue<Map.Entry<GameSessionEventListener, GameSessionContext>> eventListeners;
    private static AtomicBoolean tickOccurred = new AtomicBoolean(false);

    @BeforeAll
    static void setup() {

        final var tempChannel = new EmbeddedChannel();

        eventLoop = new GameSessionEventLoop()
                .withGameSessionListeners(eventListeners = new ConcurrentLinkedQueue<>())
                .withGlobalConnections(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE))
                .withOptions(new LinkedList<>(List.of(new AbstractMap.SimpleEntry<>(GameSessionConfigurationOption.MAX_PLAYERS, 1))))
                .withGameSessionEventListener(eventListener = new GameSessionEventListener() {

                    @Override
                    public void onErrorThrown(GameSessionContext context, Throwable t) {

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
                });
    }

    @Test
    void runTest() throws InterruptedException {

        try (final var executor = Executors.newSingleThreadExecutor()) {

            executor.submit(eventLoop);

            TimeUnit.MILLISECONDS.sleep(10);

            assertFalse(eventListeners.isEmpty());

            assertEquals(eventListener, eventListeners.poll().getKey());

            TimeUnit.MILLISECONDS.sleep(80);

            assertTrue(tickOccurred.get());
        }
    }
}
