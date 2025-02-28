package container.game.docker.ship.parents.repositories;

import container.game.docker.ship.injection.annotations.InjectSessionFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;

import java.util.List;

public class Repository<T> {
    private final Class<T> persistableClass;
    @InjectSessionFactory
    private SessionFactory sessionFactory;

    public Repository(final Class<T> persistableClass) {

        this.persistableClass = persistableClass;
    }

    public T findByIdentifier(final Long identifier){

        return sessionFactory.fromTransaction(session -> session.find(persistableClass, identifier));
    }

    /**
     * Implementation from <a href=https://www.baeldung.com/hibernate-select-all>source</a></a>.
     * @return
     */
    public List<T> findAll(){

        return sessionFactory.fromTransaction(session -> {

            final var criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(persistableClass);
            final Root<T> root = criteriaQuery.from(persistableClass);
            final var all = criteriaQuery.select(root);
            final TypedQuery<T> allQuery = session.createQuery(all);

            return allQuery.getResultList();
        });
    }

    public T store(final T entity){

        return sessionFactory.fromTransaction(session -> session.merge(entity));
    }

    public T update(final T entity){

        return sessionFactory.fromTransaction(session -> session.merge(entity));
    }

    public T delete(final Long identifier){

        final var deletedEntity = sessionFactory.fromTransaction(session -> session.find(persistableClass, identifier));

        sessionFactory.inTransaction(session -> session.remove(deletedEntity));

        return deletedEntity;
    }
}
