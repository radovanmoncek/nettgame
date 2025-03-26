package cz.radovanmoncek.ship.parents.repositories;

import cz.radovanmoncek.ship.injection.annotations.ChannelHandlerAttributeInjectee;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * This construct is an alternative to the model + service ORM mechanism, and is an attempt of solving their encapsulation
 * breaking issues, especially the model DTO design principle,
 * which is an antipattern known as the <a href=https://refactoring.guru/smells/data-class>Data Class</a>.
 * <p>
 * The idea is to, basically, combine both model and service into one singular class.
 * </p>
 * Thanks to: <a href=https://www.yegor256.com/2016/07/06/data-transfer-object.html>yegor256</a>
 * Thanks to: <a href=https://refactoring.guru/smells/data-class>refactoring.guru</a>
 * Thanks to: <a href=https://docs.jboss.org/hibernate/orm/6.6/repositories/html_single/Hibernate_Data_Repositories.html>Hibernate Repositories</a>
 *
 * @author Radovan Monček
 */
public abstract class Repository<T> {
    @ChannelHandlerAttributeInjectee
    @SuppressWarnings("unused")
    private SessionFactory sessionFactory;

    /**
     * Find an {@link jakarta.persistence.Entity} by identifier.
     *
     * @param identifier the identifier to search by.
     * @return an {@link Object} for the matching identifier, if such exists.
     */
    public Optional<T> findByIdentifier(final Long identifier) {

        return Optional.of(sessionFactory.fromTransaction(session -> session.find(returnEntityClass(), identifier)));
    }

    private Class<T> returnEntityClass() {

        return (Class<T>) ReflectionUtilities.returnTypeParameterAtIndex(getClass(), 0);
    }

    /**
     * Implementation from <a href=https://www.baeldung.com/hibernate-select-all>source</a></a>.
     *
     * @return a list of fitting entities.
     */
    public List<T> findAll() {

        return sessionFactory.fromTransaction(session -> {

            final var criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(returnEntityClass());
            final Root<T> root = criteriaQuery.from(returnEntityClass());
            final var all = criteriaQuery.select(root);
            final TypedQuery<T> allQuery = session.createQuery(all);

            return allQuery.getResultList();
        });
    }

    public Optional<T> store(final T entity) {

        return Optional.of(sessionFactory.fromTransaction(session -> session.merge(entity)));
    }

    public Optional<T> update(final T entity) {

        return Optional.of(sessionFactory.fromTransaction(session -> session.merge(entity)));
    }

    public Optional<T> delete(final Long identifier) {

        final var deletedEntity = sessionFactory.fromTransaction(session -> session.find(returnEntityClass(), identifier));

        sessionFactory.inTransaction(session -> session.remove(deletedEntity));

        return Optional.of(deletedEntity);
    }
}
