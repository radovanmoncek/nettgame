package server.game.docker.modules.usernames.facades;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import server.game.docker.modules.usernames.pdus.UsernamePDU;
import server.game.docker.ship.parents.facades.ServerFacade;

import java.util.HashMap;
import java.util.Map;

public class UsernameServerFacade extends ServerFacade<UsernamePDU> {
    private final Map<ChannelId, String> clientUsernames;

    public UsernameServerFacade() {
        clientUsernames = new HashMap<>();
    }

    /**
     * <p>
     *     This serverside method attempts to acknowledge that the given client was assigned the desired username by echoing it to the {@link Channel clientChannel},
     *     as well as internally handling its assignment.
     * </p>
     * <p>
     *     This is a default server module functionality method, overriding it without calling the super implementation cannot guarantee proper operation.
     * </p>
     * @param newClientUsername the requested username
     * @param clientChannel the request initiator
     */
    public void assignClientUsername(final String newClientUsername, final Channel clientChannel) {
        clientUsernames.put(clientChannel.id(), newClientUsername);
        final var usernamePDU = new UsernamePDU();
        usernamePDU.setNewClientUsername(newClientUsername);
        unicastPDUToClientChannel(usernamePDU, clientChannel);
    }

    public void receiveClientUsernameRequest(){
        throw new UnsupportedOperationException("This method is not implemented.");
    }

    public final String getClientUsername(final ChannelId clientChannelId) {
        return clientUsernames.get(clientChannelId);
    }

    public void removeClientUsername(final ChannelId id) {
        clientUsernames.remove(id);
    }
}
