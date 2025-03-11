package cz.radovanmoncek.ship.parents.repositories;

import cz.radovanmoncek.ship.parents.models.PersistableModel;

import java.util.List;

public interface Repository {

    List<PersistableModel> findAll();

    PersistableModel findByIdentifier(Long identifier);

    PersistableModel store(PersistableModel entity);

    PersistableModel update(PersistableModel entity);

    PersistableModel delete(Long identifier);
}
