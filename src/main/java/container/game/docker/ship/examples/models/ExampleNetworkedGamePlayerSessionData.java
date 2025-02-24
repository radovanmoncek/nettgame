package container.game.docker.ship.examples.models;

import container.game.docker.ship.parents.models.PlayerSessionData;
import io.netty.channel.ChannelId;

import java.util.Optional;
import java.util.UUID;

public class ExampleNetworkedGamePlayerSessionData implements PlayerSessionData {
    private String nickname;
    private UUID sessionUUID;
    private ChannelId playerChannelId;
    private int x, y, rotationAngle;
    private boolean wantsSessionToStop;
    private String lastChatMessage;

    @Override
    public Optional<ChannelId> retrievePlayerChannelId() {
        
        return Optional.ofNullable(playerChannelId);
    }

    @Override
    public void placePlayerChannelId(ChannelId channelId) {

        this.playerChannelId = channelId;
    }

    public Optional<String> retrieveNickname() {
        return Optional.of(nickname);
    }

    public void placeNickname(String nickname) {
        this.nickname = nickname;
    }

    public Optional<UUID> retrieveSessionUUID() {
        return Optional.ofNullable(sessionUUID);
    }

    public void placeSessionUUID(UUID sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public Optional<Boolean> retrieveWantsSessionToStop() {
        return Optional.of(wantsSessionToStop);
    }

    public void placeWantsSessionToStop(boolean wantsSessionToStop) {
        this.wantsSessionToStop = wantsSessionToStop;
    }

    public Optional<Integer> retrieveRotationAngle() {
        return Optional.of(rotationAngle);
    }

    public void placeRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public Optional<Integer> retrieveX() {
        return Optional.of(x);
    }

    public void placeX(int x) {
        this.x = x;
    }

    public Optional<Integer> retrieveY() {
        return Optional.of(y);
    }

    public void placeY(int y) {
        this.y = y;
    }

    public void placeLastChatMessage(final String message) {

        this.lastChatMessage = message;
    }

    public Optional<String> retrieveLastChatMessage() {

        return Optional.of(lastChatMessage);
    }
}
