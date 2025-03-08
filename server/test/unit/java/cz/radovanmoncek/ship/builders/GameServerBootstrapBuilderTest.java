package cz.radovanmoncek.ship.builders;

import cz.radovanmoncek.ship.parents.services.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServerBootstrapBuilderTest {
    private static GameServerBootstrapBuilder gameServerBootstrapBuilder;

    @BeforeEach
    void setUp() {

        gameServerBootstrapBuilder = new GameServerBootstrapBuilder();
    }

    @Test
    void build() {

        final var result = gameServerBootstrapBuilder.build();

        assertNotNull(result);
    }

    @Test
    void reset() {
    }

    @Test
    void buildPort() {
    }

    @Test
    void buildInternetProtocolAddress() {
    }

    @Test
    void buildChannelHandlerCreator() {
    }

    @Test
    void buildPersistableClass() {
    }

    @Test
    void buildService() {

        gameServerBootstrapBuilder
                .buildService(new Service<>(Object.class) {

                    @Override
                    public Object findByIdentifier(Long identifier) {
                        return null;
                    }

                    @Override
                    public List<Object> findAll() {
                        return List.of();
                    }

                    @Override
                    public Object store(Object entity) {
                        return null;
                    }

                    @Override
                    public Object update(Long identifier, Object entity) {
                        return null;
                    }

                    @Override
                    public Object delete(Long identifier) {
                        return null;
                    }
                })
                .build();
    }

    @Test
    void buildProtocolSchema() {
    }

    @Test
    void buildLogLevel() {
    }
}
