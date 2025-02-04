package server.game.docker.modules.player.facades;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import server.game.docker.modules.player.pdus.NicknameProtocolDataUnit;
import server.game.docker.ship.parents.facades.ChannelGroupHandler;

import java.util.HashMap;
import java.util.Map;

public class PlayerChannelGroupHandler extends ChannelGroupHandler<NicknameProtocolDataUnit> {
    private final Map<ChannelId, String> nicknames;

    public PlayerChannelGroupHandler() {
        nicknames = new HashMap<>();
    }

    /**
     * <p>
     *     This serverside method attempts to acknowledge that the given client was assigned the desired username by echoing it to the {@link Channel clientChannel},
     *     as well as internally handling its assignment.
     * </p>
     * <p>
     *     This is a default server module functionality method, overriding it without calling the super implementation will cause improper operation.
     * </p>
     * @param newClientUsername the requested username
     * @param clientChannel the request initiator
     */
    public void receiveNicknameRequest(final String newClientUsername, final Channel clientChannel) {
        nicknames.put(clientChannel.id(), newClientUsername);
        final var usernamePDU = new NicknameProtocolDataUnit(newClientUsername);
        unicastPDUToClientChannel(usernamePDU, clientChannel);
    }

    public final String getNickname(final ChannelId clientChannelId) {
        return nicknames.get(clientChannelId);
    }

    public void removeNickname(final ChannelId id) {
        nicknames.remove(id);
    }
}
