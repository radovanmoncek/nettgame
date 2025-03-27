package cz.radovanmoncek.test.ship.sessions.events;

import cz.radovanmoncek.ship.parents.models.GameSessionContext;
import cz.radovanmoncek.ship.parents.events.GameSessionEventListener;
import cz.radovanmoncek.ship.events.GameSessionEventLoop;
import cz.radovanmoncek.ship.models.GameSessionConfigurationOption;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GameSessionEventLoopTest {
    private static ChannelGroup globalConnections;
    private static GameSessionEventLoop eventLoop;
    private static GameSessionEventListener eventListener;
    private static ConcurrentLinkedQueue<Map.Entry<GameSessionEventListener, GameSessionContext>> eventListeners;

    @BeforeAll
    static void setup() {

        eventLoop = new GameSessionEventLoop()
                .withGameSessionListeners(eventListeners = new ConcurrentLinkedQueue<>())
                .withGlobalConnections(globalConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE))
                .withOptions(new LinkedList<>(List.of(new AbstractMap.SimpleEntry<>(GameSessionConfigurationOption.MAX_PLAYERS, 0))))
                .withGameSessionEventListener(eventListener = new GameSessionEventListener() {
                    @Override
                    public void onErrorThrown(GameSessionContext context, Throwable t) {

                    }

                    @Override
                    public void onInitialize(GameSessionContext context) {

                    }

                    @Override
                    public void onStart(GameSessionContext context) {

                    }

                    @Override
                    public void onServerTick(GameSessionContext context) {

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

        Executors
                .newSingleThreadExecutor()
                .submit(eventLoop);

        TimeUnit.MILLISECONDS.sleep(10);

        assertFalse(eventListeners.isEmpty());

        assertEquals(eventListener, eventListeners.poll().getKey());
    }
}
