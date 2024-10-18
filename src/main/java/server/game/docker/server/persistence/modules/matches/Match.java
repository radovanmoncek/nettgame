package server.game.docker.server.persistence.modules.matches;

import java.util.Date;

/**
 * This is a POJO DTO class serving as persistence medium for storing information about a match that had transpired
 */
public class Match {
    private Long iD;
    private Date startTime;
    private Date endTime;
    private Long sessionID;
    private Long winnerID;
}
