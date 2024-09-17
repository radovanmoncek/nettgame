package server.game.docker.server.matchmaking.models;

import java.sql.Timestamp;

/**
 * This is a POJO DTO class serving as persistence medium for storing information about a match that had transpired
 */
public class Match {
    private Long iD;
    private Timestamp startTime;
    private Timestamp endTime;
    private Long sessionID;
}
