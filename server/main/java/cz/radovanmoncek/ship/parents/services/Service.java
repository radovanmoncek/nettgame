package cz.radovanmoncek.ship.parents.services;

import cz.radovanmoncek.ship.repositories.Repository;

import java.util.List;

public abstract class Service<T> {
    protected final Repository<T> repository;

    protected Service(Class<T> persistableClass) {

        repository = new Repository<>(persistableClass);
    }

    public abstract T findByIdentifier(final Long identifier);

    public abstract List<T> findAll();

    public abstract T store(final T entity);

    public abstract T update(final Long identifier, final T entity);

    public abstract T delete(final Long identifier);
}
