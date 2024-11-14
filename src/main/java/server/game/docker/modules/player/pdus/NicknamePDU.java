package server.game.docker.modules.player.pdus;

import server.game.docker.ship.parents.pdus.PDU;

public record NicknamePDU(String nickname) implements PDU {
    public static final Byte PROTOCOL_IDENTIFIER = 1;
}
