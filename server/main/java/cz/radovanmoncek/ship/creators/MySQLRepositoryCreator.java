package cz.radovanmoncek.ship.creators;

import cz.radovanmoncek.ship.parents.creators.RepositoryCreator;
import cz.radovanmoncek.ship.parents.repositories.Repository;
import cz.radovanmoncek.ship.repositories.MySQLRepository;

public class MySQLRepositoryCreator extends RepositoryCreator {

    @Override
    public Repository createRepository() {

        return new MySQLRepository();
    }
}
