package cz.radovanmoncek.ship.builders;

import cz.radovanmoncek.ship.parents.models.PersistableModel;
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
    void buildPersistableModel() {

        gameServerBootstrapBuilder
                .buildPersistableModel(new PersistableModel() {
                    @Override
                    public PersistableModel findByIdentifier(Long identifier) {
                        return null;
                    }

                    @Override
                    public List<PersistableModel> findAll() {
                        return List.of();
                    }

                    @Override
                    public PersistableModel store() {
                        return null;
                    }

                    @Override
                    public PersistableModel update() {
                        return null;
                    }

                    @Override
                    public PersistableModel delete(Long identifier) {
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
