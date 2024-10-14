package server.game.docker.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.pdus.*;
import server.game.docker.server.GameServer;

import java.net.InetAddress;
import java.util.Map;

public final class GameSessionClientAPI {
    private final GameSessionClient gameSessionClient;
    private Channel clientChannel;
    private final Bootstrap bootstrap;
    private final InetAddress gameServerAddress;
    private final Integer gameServerPort;
    private final EventLoopGroup workerGroup;
    private final Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings;

    public GameSessionClientAPI() throws Exception {
        gameSessionClient = new GameSessionClient(new String[]{});
        clientChannel = gameSessionClient.getClientChannel();
        bootstrap = gameSessionClient.getBootstrap();
        gameServerAddress = gameSessionClient.getServerAddress();
        gameServerPort = gameSessionClient.getGameServerPort();
        workerGroup = gameSessionClient.getWorkerGroup();
        eventMappings = gameSessionClient.getEventMappings();
    }

    public GameSessionClientAPI(String [] args) throws Exception {
        gameSessionClient = new GameSessionClient(args);
        clientChannel = gameSessionClient.getClientChannel();
        bootstrap = gameSessionClient.getBootstrap();
        gameServerAddress = gameSessionClient.getServerAddress();
        gameServerPort = gameSessionClient.getGameServerPort();
        workerGroup = gameSessionClient.getWorkerGroup();
        eventMappings = gameSessionClient.getEventMappings();
    }

    /*--------API methods--------*/

    /**
     * <p>
     *     Attempts to connect to a running {@link GameServer} instance.
     * </p>
     * <p>
     *     This is a <i>blocking method</i>, it blocks the current thread upon successfully establishing a connection until the {@link #disconnect() disconnect} method is called
     *     and successfully executed.
     * </p>
     * @throws Exception if the connection fails to establish
     */
    public void connect() throws Exception {
        gameSessionClient.setClientChannel(bootstrap.connect(gameServerAddress, gameServerPort).sync().channel());
        clientChannel = gameSessionClient.getClientChannel();
        System.out.println("Connected to the server"); //todo: log4j
    }

    /**
     * Signals whether the {@link GameSessionClient} instance has a successfully established connection to a running {@link GameServer} instance.
     * @return {@code true} if the {@link GameSessionClient} is currently connected and {@code false} otherwise
     */
    public boolean isConnected(){
        return clientChannel != null && clientChannel.isActive();
    }

    public void disconnect() throws Exception {
        clientChannel.close();
        clientChannel.closeFuture().sync();
        workerGroup.shutdownGracefully();
    }

    //todo: temp class, client will be launched from JavaFX gui
//    public static void main(String[] args) throws Exception {
//        new GameClient(args);
//    }

    /**
     * <p>
     *     The ClientID value assigned to this GameClient by the server.
     * </p>
     * @return {@link Long} clientID
     */
//    public Long getClientID() {
//        return clientID;
//    }

    /*--------IoC Events--------*/
    public void setOnIDReceived(ClientAPIEventHandler<PDUID> eventHandler){
        eventMappings.put(ClientAPIEventType.CONNECTED, eventHandler);
    }

    public void setOnLobbyCreate(ClientAPIEventHandler<PDULobbyUpdate> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYCREATED, eventHandler);
    }

    public void setOnLobbyJoined(ClientAPIEventHandler<PDULobbyUpdate> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYJOINED, eventHandler);
    }

    public void setOnLobbyLeave(ClientAPIEventHandler<PDULobbyUpdate> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYLEFT, eventHandler);
    }

    public void setOnMemberJoined(ClientAPIEventHandler<PDULobbyUpdate> eventHandler){
        eventMappings.put(ClientAPIEventType.MEMBERJOINED, eventHandler);
    }

    public void setOnMemberLeft(ClientAPIEventHandler<PDULobbyUpdate> eventHandler){
        eventMappings.put(ClientAPIEventType.MEMBERLEFT, eventHandler);
    }

    public void setOnLobbyBeacon(ClientAPIEventHandler<PDULobbyBeacon> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYBEACON, eventHandler);
    }

    public void setOnLobbyChatMessageReceived(ClientAPIEventHandler<PDUChatMessage> eventHandler){
        eventMappings.put(ClientAPIEventType.LOBBYCHATMESSAGERECEIVED, eventHandler);
    }

    /*--------requests--------*/
    /**
     * Attempts to request creation of a personal lobby.
     */
    public void createLobby(){
        PDULobbyReq PDULobbyReq = new PDULobbyReq();
        PDULobbyReq.setActionFlag((byte) 0);
        gameSessionClient.sendUnicast(PDUType.LOBBYREQUEST, PDULobbyReq);
    }

    /**
     * Attempts to join a specified lobby.
     * @param lobbyID the ID of the desired lobby
     */
    public void joinLobby(Long lobbyID){
        PDULobbyReq PDULobbyReq = new PDULobbyReq();
        PDULobbyReq.setActionFlag((byte) 1);
        PDULobbyReq.setLobbyID(lobbyID);
        gameSessionClient.sendUnicast(PDUType.LOBBYREQUEST, PDULobbyReq);
    }

    /**
     * Attempts to leave current lobby.
     */
    public void leaveLobby(){
        PDULobbyReq PDULobbyRequest = new PDULobbyReq();
        PDULobbyRequest.setActionFlag((byte) 2);
        gameSessionClient.sendUnicast(PDUType.LOBBYREQUEST, PDULobbyRequest);
    }

    public void sendChatMessage(String message) {
        if (message == null)
            throw new NullPointerException("Message cannot be null");
        if (message.isBlank())
            throw new IllegalArgumentException("Message cannot be blank");
        if (message.length() > 64)
            throw new IllegalArgumentException("Message cannot be longer than 64 characters");
        PDUChatMessage PDUChatMessage = new PDUChatMessage();
        PDUChatMessage.setMessage(message);
        gameSessionClient.sendUnicast(PDUType.CHATMESSAGE, PDUChatMessage);
    }
}
