package cz.radovanmoncek.ship.repositories;

import cz.radovanmoncek.ship.injection.annotations.AttributeInjectee;
import cz.radovanmoncek.ship.parents.models.PersistableModel;
import cz.radovanmoncek.ship.parents.repositories.Repository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;

import java.util.List;

public final class MySQLRepository implements Repository {
    @AttributeInjectee
    private Class<PersistableModel> persistableModelClass;
    @AttributeInjectee
    private SessionFactory sessionFactory;

    public PersistableModel findByIdentifier(final Long identifier){

        return sessionFactory.fromTransaction(session -> session.find(persistableModelClass, identifier));
    }

    /**
     * Implementation from <a href=https://www.baeldung.com/hibernate-select-all>source</a></a>.
     * @return a list of fitting entities.
     */
    public List<PersistableModel> findAll(){

        return sessionFactory.fromTransaction(session -> {

            final var criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<PersistableModel> criteriaQuery = criteriaBuilder.createQuery(persistableModelClass);
            final Root<PersistableModel> root = criteriaQuery.from(persistableModelClass);
            final var all = criteriaQuery.select(root);
            final TypedQuery<PersistableModel> allQuery = session.createQuery(all);

            return allQuery.getResultList();
        });
    }

    public PersistableModel store(final PersistableModel entity){

        return sessionFactory.fromTransaction(session -> session.merge(entity));
    }

    public PersistableModel update(final PersistableModel entity){

        return sessionFactory.fromTransaction(session -> session.merge(entity));
    }

    public PersistableModel delete(final Long identifier){

        final var deletedEntity = sessionFactory.fromTransaction(session -> session.find(persistableModelClass, identifier));

        sessionFactory.inTransaction(session -> session.remove(deletedEntity));

        return deletedEntity;
    }
}
