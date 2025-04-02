package cz.radovanmoncek.ship.bay.events;

import cz.radovanmoncek.ship.bay.events.DefaultGameSessionContext;
import cz.radovanmoncek.ship.bay.utilities.logging.LoggingUtilities;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultGameSessionContextTest {
    private static DefaultGameSessionContext defaultGameSessionContext;
    private static ChannelGroup globalConnections, contextConnections;
    private static LinkedBlockingQueue<Channel> pendingChannels;

    @BeforeAll
    static void setup() {

        LoggingUtilities.enableGlobalLoggingLevel(Level.ALL);

        globalConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        contextConnections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        pendingChannels = new LinkedBlockingQueue<>();
        defaultGameSessionContext = new DefaultGameSessionContext(globalConnections, contextConnections, pendingChannels);
    }

    @BeforeEach
    void cleanup() {

        globalConnections.close();
        contextConnections.close();
        pendingChannels.clear();
    }

    @Test
    void broadcast() throws InterruptedException {

        final var channel1 = new EmbeddedChannel();
        //final var channel2 = new EmbeddedChannel();
        final var message = new Object();

        contextConnections.add(channel1);
        //contextConnections.add(channel2);
        defaultGameSessionContext.broadcast(message);

        TimeUnit.MILLISECONDS.sleep(10);

        assertEquals(message, channel1.readOutbound());
        //assertEquals(message, channel2.readOutbound());
    }

    @Test
    void performOnAllConnections() {

        final var connection = new EmbeddedChannel();
        final var desiredConnection = new AtomicReference<Channel>();

        contextConnections.add(connection);
        defaultGameSessionContext.performOnAllConnections(desiredConnection::set);

        assertEquals(connection, desiredConnection.get());
    }

    @Test
    void registerPlayerConnection() {

        final var connection = new EmbeddedChannel();

        defaultGameSessionContext.registerPlayerConnection(connection);

        assertEquals(1, pendingChannels.size());

        assertTrue(pendingChannels.contains(connection));
    }

    @Test
    void unregisterPlayerConnection() {

        final var connection = new EmbeddedChannel();

        globalConnections.add(connection);

        defaultGameSessionContext.registerPlayerConnection(connection);

        assertEquals(1, pendingChannels.size());

        assertTrue(pendingChannels.contains(connection));

        defaultGameSessionContext.unregisterPlayerConnection(connection);

        assertEquals(0, globalConnections.size());

        assertFalse(globalConnections.contains(connection));
    }

    @Test
    void performOnAllConnectionsWithIndex() {

        final var connection = new EmbeddedChannel();
        final var desiredConnection = new AtomicReference<Channel>();

        contextConnections.add(connection);
        defaultGameSessionContext.performOnAllConnectionsWithIndex((channel, index) -> desiredConnection.set(channel));

        assertEquals(connection, desiredConnection.get());
    }
}
