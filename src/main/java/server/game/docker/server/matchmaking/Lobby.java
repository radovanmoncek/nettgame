package server.game.docker.server.matchmaking;

import io.netty.channel.ChannelId;

import java.util.*;

public class Lobby {
//    private final ChannelGroup connectedClients;
    private final Set<ChannelId> clientIDs;
    private final Byte maxSize;
    private final ChannelId leaderClientID;
//    private final LobbyChat lobbyChat;

    public Lobby(Byte maxSize, ChannelId leaderClientID) {
//        connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        clientIDs = new HashSet<>(List.of(leaderClientID));
        this.maxSize = maxSize;
        this.leaderClientID = leaderClientID;
//        handler;
//        lobbyChat = new LobbyChat(); chat exists as only a concept
    }

    public Byte getCurrentSize(){
        //Lobbies bigger than the bounds of a Byte are not expected to be supported
        return (byte) /*connectedClients*/clientIDs.size();
    }

    public void join(/*Channel clientChannel,*/ ChannelId clientID){
        if(/*connectedClients clientIDs.size() >= maxSize*/isFull())
            return;
//        connectedClients.add(clientChannel);
        clientIDs.add(clientID);
    }

    public void leave(/*Channel clientChannel,*/ ChannelId clientID){
//        if(clientID.equals(leaderClientID) && !clientIDs.isEmpty()){
//            leaderClientID = clientID;
//        } todo: externally destroy lobby
//        connectedClients.remove(clientChannel);
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
