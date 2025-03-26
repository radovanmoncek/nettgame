package cz.radovanmoncek.server.modules.games.models;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "GameHistories")
public class GameHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    @SuppressWarnings("unused")
    private Long iD;
    @Column(name="gameSessionUUID")
    private String gameSessionUUID;
    @Column(name="endTime")
    private Timestamp endTime;
    @Column(name = "player1Name", length = 10)
    private String player1Name;
    @Column(name = "player2Name", length = 10)
    private String player2Name;
    @Lob
    @Column(name = "totalScore")
    private Long totalScore;

    public void setGameSessionUUID(String gameSessionUUID) {
        this.gameSessionUUID = gameSessionUUID;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public void setTotalScore(Long totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public String toString() {

        return String.format(
                """
                --------------------------
                | %s | %s | %s | %s | %s |
                --------------------------
                """,
                gameSessionUUID,
                endTime,
                player1Name,
                player2Name,
                totalScore
        );
    }
}
