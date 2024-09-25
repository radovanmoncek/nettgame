package server.game.docker.server.matchmaking;

import io.netty.channel.ChannelId;

import java.util.*;

public class Lobby {
    private final Set<ChannelId> clientIDs;
    private final Byte maxSize;
    private final ChannelId leaderClientID;

    public Lobby(Byte maxSize, ChannelId leaderClientID) {
        clientIDs = new HashSet<>(List.of(leaderClientID));
        this.maxSize = maxSize;
        this.leaderClientID = leaderClientID;
//      chat exists as only a concept
    }

    /**
     * Lobbies bigger than the bounds of a Byte {@code Byte.MAX_VALUE} are not expected to be supported
     * @return {@link Byte} the size of this lobby
     */
    public Byte getCurrentSize(){
        return (byte) clientIDs.size();
    }

    public void join(ChannelId clientID){
        if(isFull())
            return;
        clientIDs.add(clientID);
    }

    public void leave(ChannelId clientID){
        clientIDs.remove(clientID);
    }

    public void appendLobbyChatMessage(String message){

    }

    public ChannelId getLeaderID() {
        return leaderClientID;
    }

    public Byte getMaxSize() {
        return maxSize;
    }

    public Set<ChannelId> getClientIDs() {
        return clientIDs;
    }

    public boolean isFull() {
        return clientIDs.size() == maxSize;
    }

    public static class LobbyChat {
        private final List<String> lobbyMessageHistory;

        public LobbyChat() {
            lobbyMessageHistory = new LinkedList<>();
        }

        public void appendMessage(String message) {
            lobbyMessageHistory.add(message);
        }
    }
}
