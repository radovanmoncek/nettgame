package server.game.docker;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import server.game.docker.modules.requests.handlers.PDULobbyInboundHandler.Lobby;
import server.game.docker.modules.session.handlers.GameSessionHandler;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.*;

/**
 * <p>
 *     The root class of the DockerGameServer, it initializes and encapsulates the Netty protocol server and its functionality.
 * </p>
 * <p>
 *     Please make sure to note that because of the multi-threaded nature of {@link EventLoopGroup} workers,
 *     any methods utilizing their functionality are declared as synchronized.
 * </p>
 */
public final class GameServer {
    private final int port;
    private final Map<Long, Thread> sessions;
    private final GameServerInitializer.RouterHandler multiPipeline;
    private final ChannelGroup managedClients;
    private final Set<ChannelId> unassignedDomain;
    private final Map<ChannelId, Long> lobbyDomain;
    private final Map<ChannelId, Long> sessionDomain; //todo: Docker in phase 4
    private final Map<Long, Lobby> lobbyLookup;
    /**
     * GameSession identified by Long Lobby id
     * Because Session ID = Match ID (for persistence only)
     */
    private final Map<Long, GameSessionHandler> sessionLookup;
    /**
     * Arbitrary channel / client ID - transformer for debugging purposes (later will originate from database)
     */
    private final Map<ChannelId, Long> channelIDClientIDLookup;
    private Long autoIncrementClientID = 1L;
    private Long autoIncrementDebugClientID = 1L;
    private final Map<ChannelId, Channel> channelLookup;

    public GameServer(int port) {
        this.port = port;
        managedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        unassignedDomain = new HashSet<>();
        lobbyDomain = new HashMap<>();
        sessionDomain = new HashMap<>();
        lobbyLookup = new HashMap<>();
        sessionLookup = new HashMap<>();
        channelIDClientIDLookup = new HashMap<>();
        multiPipeline = new GameServerInitializer.RouterHandler();
        channelLookup = new HashMap<>();
        sessions = new HashMap<>();
    }

    public GameServer(String [] args) {
        this(4321);
    }

    public void run() throws Exception {
        new GameServerInitializer().init(port, this);
    }

    /*--------Thread safe methods--------*/

    public synchronized Optional<Channel> findDomainChannel(ChannelId chID) {
        return unassignedDomain.stream().map(channelLookup::get).filter(c -> c.id().equals(chID)).findAny()
                .or(() -> lobbyDomain.keySet().stream().map(channelLookup::get).filter(c -> c.id().equals(chID)).findAny())
                .or(() -> sessionDomain.keySet().stream().map(channelLookup::get).filter(c -> c.id().equals(chID)).findAny());
    }

    public synchronized void sendUnicast(PDUType type, PDU protocolDataUnit, Channel channel) {
        unassignedDomain.stream().map(channelLookup::get).filter(c -> c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.route(type, protocolDataUnit, c));
        lobbyDomain.keySet().stream().map(channelLookup::get).filter(c -> c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.route(type, protocolDataUnit, c));
        sessionDomain.keySet().stream().map(channelLookup::get).filter(c -> c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.route(type ,protocolDataUnit, c));
    }

    private synchronized void sendBroadcast(PDUType type, PDU protocolDataUnit, Channel channel) {
        sendBroadcastUnassigned(PDUType.LOBBYBEACON, protocolDataUnit, channel);
        sendBroadcastLobby(type, protocolDataUnit, channel);
        sessionDomain.keySet().stream().map(channelLookup::get).filter(c -> !c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.route(type, protocolDataUnit, c));
    }

    public synchronized void sendBroadcastUnassigned(PDUType type, PDU protocolDataUnit, Channel channel) {
        unassignedDomain.stream().map(channelLookup::get).filter(c -> !c.remoteAddress().equals(channel.remoteAddress())).forEach(c -> multiPipeline.route(type, protocolDataUnit, c));
    }

    public synchronized void sendBroadcastLobby(PDUType type, PDU protocolDataUnit, Channel channel) {
        lobbyDomain.entrySet().stream()
                .filter(e ->
                        !e.getKey().equals(channel.id()) /*&& (!(protocolDataUnit instanceof PDULobbyBeacon) || ((PDULobbyBeacon) protocolDataUnit).getLobbyID() != lookupLobbyIDForChannelID(channel.id()))*/
                )
                .forEach(e -> multiPipeline.route(type, protocolDataUnit, channelLookup.get(e.getKey())));
    }

    public synchronized void sendMulticastLobby(PDUType type, PDU protocolDataUnit, Channel channel) {
        lobbyDomain.entrySet().stream().filter(e -> managedClients.find(e.getKey()).remoteAddress().equals(channel.remoteAddress())).map(Map.Entry::getValue).forEach(l ->
                lobbyDomain.entrySet().stream().
                        filter(e -> !managedClients.find(e.getKey()).remoteAddress().equals(channel.remoteAddress()) && e.getValue().equals(l)).map(Map.Entry::getKey)
                        .map(channelLookup::get)
                        .forEach(c -> multiPipeline.route(type, protocolDataUnit, c))
        );
    }

    public synchronized Long transformChID(ChannelId chID) {
        return channelIDClientIDLookup.get(chID);
    }

    public synchronized void unassignChannel(ChannelId channelID) {
        assignChannel(channelID);
        unassignLobbyChannel(channelID);
        sessionDomain.remove(channelID);
    }

    public synchronized Long unassignLobbyChannel(ChannelId channelID) {
        return lobbyDomain.remove(channelID);
    }

    public synchronized Long getNextLobbyID(){
        return autoIncrementClientID++;
    }

    public synchronized Long getNextDebugClientID() {
        return autoIncrementDebugClientID++;
    }

    public synchronized boolean isChannelInLobby(Long lobbyID, ChannelId channelID) {
        return lobbyLookup.get(lobbyID).getClientIDs().contains(channelID);
    }

    public synchronized void addChannelToLobby(Long lobbyID, ChannelId channelID) {
        lobbyLookup.get(lobbyID).join(channelID);
    }

    public synchronized void removeChannelFromLobby(Long lobbyID, ChannelId channelID) {
        lobbyLookup.get(lobbyID).leave(channelID);
    }

    public synchronized void putLobby(Long lobbyID, Lobby lobby) {
        lobbyLookup.put(lobbyID, lobby);
    }

    public synchronized void removeLobby(Long lobbyID) {
        lobbyLookup.remove(lobbyID);
    }

    public synchronized void startManagingClient(Channel channel) {
        managedClients.add(channel);
        channelLookup.put(channel.id(), channel);
    }

    public synchronized void addToUnassigned(ChannelId channelId) {
        unassignedDomain.add(channelId);
    }

    public synchronized boolean isChannelInLobbyDomain(Channel channel) {
        return lobbyDomain.keySet().stream().map(channelLookup::get).map(Channel::remoteAddress).anyMatch(channel.remoteAddress()::equals);
    }

    public synchronized boolean isChannelInSessionDomain(Channel channel) {
        return sessionDomain.keySet().stream().map(channelLookup::get).map(Channel::remoteAddress).anyMatch(channel.remoteAddress()::equals);
    }

    public synchronized Channel findUnassignedChannel(Channel channel) {
        return unassignedDomain.stream().map(channelLookup::get).filter(c -> channel.remoteAddress().equals(c.remoteAddress())).findAny().orElse(null);
    }

    public synchronized void assignChannelToLobbyDomain(ChannelId id, Long lobbyID) {
        unassignChannel(id);
        lobbyDomain.put(id, lobbyID);
    }

    public synchronized Collection<ChannelId> getLobbyMembers(Long lobbyID){
        return lobbyLookup.get(lobbyID).getClientIDs();
    }

    public synchronized Collection<Long> getLobbyMembersTransformed(Long lobbyID) {
        return lobbyLookup.get(lobbyID).getClientIDs().stream().map(this::transformChID).toList();
    }

    public synchronized Channel findLobbyAssignedChannel(Channel channel) {
        return lobbyDomain.keySet().stream().map(channelLookup::get).filter(c -> channel.remoteAddress().equals(c.remoteAddress())).findAny().orElse(null);
    }

    public synchronized boolean assignChannel(ChannelId channelID) {
        return unassignedDomain.remove(channelID);
    }

    public synchronized Boolean isChannelLobbyLeader(Long lobbyID, ChannelId id) {
        return lobbyLookup.get(lobbyID).getLeaderID().compareTo(id) == 0;
    }

    public synchronized boolean existsLobbyWithID(Long lobbyID) {
        return lobbyLookup.get(lobbyID) != null;
    }

    public synchronized boolean isLobbyFull(Long lobbyID) {
        return lobbyLookup.get(lobbyID).isFull();
    }

    public synchronized Optional<Channel> getLobbyLeaderChannel(long lobbyID) {
        return findDomainChannel(lobbyLookup.get(lobbyID).getLeaderID());
    }

    public synchronized boolean hasAnyLobbies() {
        return !lobbyLookup.isEmpty();
    }

    public synchronized Collection<Long> accessLobbyLookupIDs() {
        return Collections.unmodifiableCollection(lobbyLookup.keySet());
    }

    public synchronized void putDebugChannelIDtoLongMapping(ChannelId channelID, Long newClientID) {
        channelIDClientIDLookup.put(channelID, newClientID);
    }

    public synchronized Long removeDebugChannelIDEntry(ChannelId id) {
        return channelIDClientIDLookup.remove(id);
    }

    public synchronized Byte accessMaxOccupancyForLobbyID(Long lobbyID) {
        return lobbyLookup.get(lobbyID).getMaxSize();
    }

    public synchronized void stopManagingChannel(Channel channel) {
        managedClients.remove(channel);
        channelLookup.remove(channel.id());
    }

    public synchronized Long lookupLobbyIDForChannelID(ChannelId channelID) {
        return lobbyDomain.get(channelID);
    }
}
