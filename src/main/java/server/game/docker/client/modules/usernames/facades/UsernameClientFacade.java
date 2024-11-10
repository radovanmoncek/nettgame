package server.game.docker.client.modules.usernames.facades;

import server.game.docker.client.ship.parents.ClientFacade;
import server.game.docker.modules.usernames.pdus.UsernamePDU;

public class UsernameClientFacade extends ClientFacade<UsernamePDU> {
    private String clientUsername;

    public void requestUsername(final String newUsername){
        if(newUsername.length() > 8)
            throw new IllegalArgumentException("Username's length exceeds 8 characters");
        final var usernamePDU = new UsernamePDU();
        usernamePDU.setNewClientUsername(newUsername);
        unicastPDUToServerChannel(usernamePDU);
    }

    /**
     *
     * @param newUsername
     */
    public void receiveNewUsername(final String newUsername){
        clientUsername = newUsername;
    }

    public final String getClientUsername() {
        return clientUsername;
    }
}
