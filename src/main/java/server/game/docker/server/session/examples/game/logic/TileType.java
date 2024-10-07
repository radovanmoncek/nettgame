package server.game.docker.server.session.examples.game.logic;

public enum TileType {
    NEXUS(Byte.MAX_VALUE), NEXUS2(Byte.MIN_VALUE), RIVER((byte) -1), RESOURCENODE((byte) 1), GOLDMINE((byte) 2), BRIDGE((byte) (Byte.MAX_VALUE - 1)), BLANK((byte) 0);

    private final Byte tileID;

    TileType(Byte tileID) {
        this.tileID = tileID;
    }

    public Byte getTileID() {
        return tileID;
    }
}
