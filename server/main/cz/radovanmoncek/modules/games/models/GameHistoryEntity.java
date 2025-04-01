package cz.radovanmoncek.modules.games.models;

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
    @Column(name = "length")
    private Long length;

    public void setGameSessionUUID(String gameSessionUUID) {

        if(gameSessionUUID.isBlank() || gameSessionUUID.length() != 36)
            return;

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

    public void setLength(Long length) {

        this.length = Math.max(0, length);
    }

    /*
     * Thanks to: https://stackoverflow.com/questions/8369708/limiting-the-number-of-characters-in-a-string-and-chopping-off-the-rest
     */
    @Override
    public String toString() {//todo: generic printer in Entity interface

        return String.format(
                """
                +----------------+----------------+----------------+----------------+----------------+
                | %14.14s | %14.14s | %14.14s | %14.14s | %14.14s |
                +----------------+----------------+----------------+----------------+----------------+
                | %14.14s | %14.14s | %14.14s | %14.14s | %14.14s |
                +----------------+----------------+----------------+----------------+----------------+
                """,
                "gameSessionUUID",
                "endTime",
                "player1Name",
                "player2Name",
                "length",
                gameSessionUUID,
                endTime,
                player1Name,
                player2Name,
                length
        );
    }
}
