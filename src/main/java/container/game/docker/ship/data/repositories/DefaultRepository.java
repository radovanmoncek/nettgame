package container.game.docker.ship.data.repositories;

import container.game.docker.ship.parents.repositories.Repository;
import org.hibernate.Session;

import java.util.List;

public class DefaultRepository implements Repository<Object> {
    private Session session;

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
}
