package client.game.docker.modules.examples.sessions.models;

import container.game.docker.ship.examples.compiled.schemas.GameStateRequest;
import container.game.docker.ship.parents.models.FlatBufferSerializable;

public record GameStateRequestFlatBuffersSerializable(int x, int y, int rotationAngle, String name, byte gameStatus, String gameCode) implements FlatBufferSerializable<GameStateRequest> {

    @Override
    public Class<GameStateRequest> getSchemaClass() {

        return GameStateRequest.class;
    }
}
