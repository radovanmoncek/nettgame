package cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.handlers;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.utilities.reflection.ReflectionUtilities;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.ChannelGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelGroupHandlerTest {
    private static ChannelGroupHandler<Table> channelGroupHandler;
    private static EmbeddedChannel channel;
    private static ChannelGroup clientChannels;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {

        channelGroupHandler = new ChannelGroupHandler<>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Table msg) {

            }
        };
        channel = new EmbeddedChannel(channelGroupHandler);
        clientChannels = (ChannelGroup) ReflectionUtilities.returnValueOnFieldReflectively(channelGroupHandler, "clientChannels");
    }

    @Test
    void handlerAdded() throws InterruptedException {

        TimeUnit.MILLISECONDS.sleep(10);

        channel.advanceTimeBy(10, TimeUnit.SECONDS);

        assertFalse(clientChannels.isEmpty());

        assertTrue(clientChannels.stream().anyMatch(channel -> ChannelGroupHandlerTest.channel.compareTo(channel) == 0));
    }

    @Test
    @Disabled("todo")
    void multicast() {
    }

    @Test
    @Disabled("todo")
    void broadcast() {
    }
}
