package container.game.docker.ship.parents.repositories;

import java.util.List;

public interface Repository<T> {

    T findByIdentifier(final Long identifier);

    List<T> findAll();

    T store(final T entity);

    T update(final Long identifier, final T entity);

    T delete(final Long identifier);
}
