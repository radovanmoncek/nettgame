package cz.radovanmoncek.ship.parents.models;

import cz.radovanmoncek.ship.injection.annotations.AttributeInjectee;
import cz.radovanmoncek.ship.parents.repositories.Repository;

import java.util.List;

/**
 * Thanks to: <a href=https://www.yegor256.com/2016/07/06/data-transfer-object.html>yegor256</a>
 * Thanks to: <a href=https://refactoring.guru/smells/data-class>refactoring.guru</a>
 */
public abstract class PersistableModel {
    @AttributeInjectee
    protected Repository repository;

    public abstract PersistableModel findByIdentifier(final Long identifier);

    public abstract List<PersistableModel> findAll();

    public abstract PersistableModel store();

    public abstract PersistableModel update();

    public abstract PersistableModel delete(final Long identifier);
}
