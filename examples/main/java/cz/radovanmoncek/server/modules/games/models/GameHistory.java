package cz.radovanmoncek.server.modules.games.models;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "GameHistories")
public class GameHistory {
    @Id
    @GeneratedValue
    @Column(name="id")
    private Long iD;
    @Column(name="gameSessionUUID")
    private String gameSessionUUID;
    @Column(name="endTime")
    private Timestamp endTime;

    public void setiD(Long iD) {
        this.iD = iD;
    }

    public Long getiD() {
        return iD;
    }
}
