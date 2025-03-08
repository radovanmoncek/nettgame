package cz.radovanmoncek.server.modules.games.services;

import cz.radovanmoncek.server.modules.games.models.GameHistory;
import cz.radovanmoncek.ship.parents.services.Service;

import java.util.List;

public class ExampleGameHistoryService extends Service<GameHistory> {

    public ExampleGameHistoryService() {

        super(GameHistory.class);
    }

    @Override
    public GameHistory findByIdentifier(Long identifier) {

        return repository.findByIdentifier(identifier);
    }

    @Override
    public List<GameHistory> findAll() {
        return repository.findAll();
    }

    @Override
    public GameHistory store(GameHistory entity) {
        return repository.store(entity);
    }

    @Override
    public GameHistory update(Long identifier, GameHistory entity) {
        return repository.store(entity);
    }

    @Override
    public GameHistory delete(Long identifier) {
        return repository.delete(identifier);
    }
}
