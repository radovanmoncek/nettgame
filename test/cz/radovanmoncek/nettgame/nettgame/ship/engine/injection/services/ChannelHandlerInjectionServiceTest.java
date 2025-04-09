package cz.radovanmoncek.nettgame.nettgame.ship.engine.injection.services;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.nettgame.nettgame.ship.deck.creators.ChannelHandlerCreator;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.handlers.ChannelGroupHandler;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.repositories.Repository;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.utilities.logging.LoggingUtilities;
import cz.radovanmoncek.nettgame.nettgame.ship.bay.utilities.reflection.ReflectionUtilities;
import cz.radovanmoncek.nettgame.nettgame.ship.engine.injection.annotations.ChannelHandlerAttributeInjectee;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.hibernate.engine.spi.SessionFactoryDelegatingImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelHandlerInjectionServiceTest {

    @BeforeAll
    static void setup() {

        LoggingUtilities.enableGlobalLoggingLevel(Level.ALL);
    }

    @Test
    void returnInjectedChannelHandlerCreators() throws NoSuchFieldException, IllegalAccessException {

        final var channelHandlerCreators = new LinkedList<ChannelHandlerCreator>();

        channelHandlerCreators.add(new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new ChannelGroupHandler<>() {
                    @ChannelHandlerAttributeInjectee
                    @SuppressWarnings("unused")
                    private Repository<Object> repository;

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Table msg) {

                    }
                };
            }
        });

        final var repository = new Repository<>() {};
        final var sessionFactory = new SessionFactoryDelegatingImpl(null);

        final var injectedChannelHandlerCreators = new ChannelHandlerInjectionService(sessionFactory, new LinkedList<>(List.of(repository)))
                .returnInjectedChannelHandlerCreators(channelHandlerCreators);

        assertEquals(repository, ReflectionUtilities.returnValueOnFieldReflectively(injectedChannelHandlerCreators.getFirst().newProduct(), "repository"));
        assertEquals(sessionFactory, ReflectionUtilities.returnValueOnFieldReflectively(ReflectionUtilities.returnValueOnFieldReflectively(injectedChannelHandlerCreators.getFirst().newProduct(), "repository"), "sessionFactory"));
    }
}
