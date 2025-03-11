package cz.radovanmoncek.server.modules.games.models;

import cz.radovanmoncek.ship.parents.models.PersistableModel;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "GameHistories")
public class ExampleGameHistoryPersistableModel extends PersistableModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long iD;
    @Column(name="gameSessionUUID")
    private String gameSessionUUID;
    @Column(name="endTime")
    private Timestamp endTime;

    @Override
    public PersistableModel findByIdentifier(Long identifier) {

        return repository.findByIdentifier(identifier);
    }

    @Override
    public List<PersistableModel> findAll() {
        return repository.findAll();
    }

    @Override
    public PersistableModel store() {
        return repository.store(this);
    }

    @Override
    public PersistableModel update() {
        return repository.update(this);
    }

    @Override
    public PersistableModel delete(Long identifier) {
        return repository.delete(identifier);
    }

    public PersistableModel store(String gameSessionUUID, Timestamp endTime) {

        this.gameSessionUUID = gameSessionUUID;
        this.endTime = endTime;

        return store();
    }
}
