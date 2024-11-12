package server.game.docker.client.modules.player.facades;

import server.game.docker.client.ship.parents.facades.ClientFacade;
import server.game.docker.modules.player.pdus.NicknamePDU;

public class PlayerClientFacade extends ClientFacade<NicknamePDU> {
    /**
     *
     * @param newNickname
     */
    public void requestNickname(final String newNickname){
        if(newNickname.length() > 8)
            throw new IllegalArgumentException("Player name's length exceeds 8 characters");
        final var nicknamePDU = new NicknamePDU();
        nicknamePDU.setNewClientUsername(newNickname);
        unicastPDUToServerChannel(nicknamePDU);
    }

    /**
     *
     * @param newNickname
     */
    public void receiveNewNickname(final String newNickname){
        throw new UnsupportedOperationException("Method receiveNewUsername is not implemented.");
    }
}
