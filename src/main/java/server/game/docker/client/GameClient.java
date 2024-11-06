package server.game.docker.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.GameServer;
import server.game.docker.GameServerInitializer;
import server.game.docker.client.modules.ids.decoders.IDDecoder;
import server.game.docker.client.modules.ids.handlers.ClientIDHandler;
import server.game.docker.client.modules.requests.encoders.LobbyReqEncoder;
import server.game.docker.client.modules.requests.facades.LobbyReqClientFacade;
import server.game.docker.modules.beacons.pdus.PDULobbyBeacon;
import server.game.docker.modules.ids.pdus.PDUID;
import server.game.docker.modules.messages.pdus.PDUChatMessage;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.modules.updates.pdus.PDULobbyUpdate;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class GameClient {
    private Bootstrap bootstrap;
    private final InetAddress gameServerAddress;
    private final int gameServerPort;
    private final EventLoopGroup workerGroup;
    private final GameServerInitializer.RouterHandler multiPipeline;
    private final Map<ClientAPIEventType, ClientAPIEventHandler<? extends PDU>> eventMappings;
    private Channel clientChannel;
    private Long assignedID;
    private LobbyReqClientFacade lobbyReqClientFacade;

    //    todo: private Long clientID; ?
    private GameClient() throws Exception {
        gameServerAddress = InetAddress.getByName("127.0.0.1");
        gameServerPort = 4321;
        workerGroup = new NioEventLoopGroup();
        multiPipeline = new GameServerInitializer.RouterHandler();
        eventMappings = new HashMap<>();
        new ClientInitializer(clientChannel, eventMappings, this, multiPipeline).init();
        try {
            bootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addFirst(
                                    new LoggingHandler(LogLevel.ERROR),
                                    new IDDecoder(),
                                    new ClientIDHandler(GameClient.this),
                                    new LobbyReqEncoder()
                            );
                        }
                    });
        }
        catch (Exception e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
    }
    public void sendUnicast(PDUType type, PDU protocolDataUnit) {
        multiPipeline.route(type, protocolDataUnit, clientChannel);
    }

    //todo: singleton !!!!
    public static GameClient getInstance() throws Exception {
        return new GameClient();
    }

    public GameClient withLobbyReqFacade(final LobbyReqClientFacade facade) {
        if(lobbyReqClientFacade != null)
            return this;
        lobbyReqClientFacade = facade;
        return this;
    }

    /*public ChannelFuture sendUnicast() {
        return clientChannel.writeAndFlush(new Object());
    }*/

    public <T extends PDU> void checkAndCallHandler(ClientAPIEventType eventType, T protocolDataUnit) {
        ClientAPIEventHandler<?> h = eventMappings.get(eventType);
//        Type t = h.getClass().getGenericInterfaces()[0];
//        if(((ParameterizedType) h.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].equals(((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0]))
        ((ClientAPIEventHandler<T>) h).handle(protocolDataUnit);
//        return null;
    }

    public final Channel getClientChannel() {
        return clientChannel;
    }

    public final Bootstrap getBootstrap() {
        return bootstrap;
    }

    public final InetAddress getServerAddress() {
        return gameServerAddress;
    }

    public int getGameServerPort() {
        return gameServerPort;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public GameServerInitializer.RouterHandler getMultiPipeline() {
        return multiPipeline;
    }

    public Map<ClientAPIEventType, ClientAPIEventHandler<?>> getEventMappings() {
        return eventMappings;
    }

    public void setClientChannel(Channel clientChannel) {
        this.clientChannel = clientChannel;
        Stream.of(lobbyReqClientFacade.getClass().getDeclaredFields()).filter(field -> field.getType().equals(Channel.class)).findAny().ifPresent(field -> {
            field.setAccessible(true);
            try {
                field.set(lobbyReqClientFacade, clientChannel);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public Long getAssignedID() {
        return assignedID;
    }

    public void setAssignedID(Long assignedID) {
        this.assignedID = assignedID;
    }

    public LobbyReqClientFacade getLobbyReqFacade() {
        return lobbyReqClientFacade;
    }

    @FunctionalInterface
    public static interface ClientAPIEventHandler<T extends PDU> {
        void handle(T data);
    }

    public enum ClientAPIEventType {
        CONNECTED, LOBBYCREATED, LOBBYJOINED, LOBBYLEFT, LOBBYBEACON, LOBBYCHATMESSAGERECEIVED, MEMBERJOINED, MEMBERLEFT
    }

    public static final class GameSessionClientAPI {
        private final GameClient gameClient;
        private Channel clientChannel;
        private final Bootstrap bootstrap;
        private final InetAddress gameServerAddress;
        private final Integer gameServerPort;
        private final EventLoopGroup workerGroup;
        private final Map<ClientAPIEventType, ClientAPIEventHandler<?>> eventMappings;

        public GameSessionClientAPI() throws Exception {
            gameClient = new GameClient();
            clientChannel = gameClient.getClientChannel();
            bootstrap = gameClient.getBootstrap();
            gameServerAddress = gameClient.getServerAddress();
            gameServerPort = gameClient.getGameServerPort();
            workerGroup = gameClient.getWorkerGroup();
            eventMappings = gameClient.getEventMappings();
        }

        public GameSessionClientAPI(String [] args) throws Exception {
            gameClient = new GameClient();
            clientChannel = gameClient.getClientChannel();
            bootstrap = gameClient.getBootstrap();
            gameServerAddress = gameClient.getServerAddress();
            gameServerPort = gameClient.getGameServerPort();
            workerGroup = gameClient.getWorkerGroup();
            eventMappings = gameClient.getEventMappings();
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
            gameClient.setClientChannel(bootstrap.connect(gameServerAddress, gameServerPort).sync().channel());
            clientChannel = gameClient.getClientChannel();
            System.out.println("Connected to the server"); //todo: log4j
        }

        /**
         * Signals whether the {@link GameClient} instance has a successfully established connection to a running {@link GameServer} instance.
         * @return {@code true} if the {@link GameClient} is currently connected and {@code false} otherwise
         */
        public boolean isConnected(){
            return clientChannel != null && clientChannel.isActive();
        }

        public void disconnect() throws Exception {
            assert clientChannel != null;
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
            gameClient.sendUnicast(PDUType.LOBBYREQUEST, PDULobbyReq);
        }

        /**
         * Attempts to join a specified lobby.
         * @param lobbyID the ID of the desired lobby
         */
        public void joinLobby(Long lobbyID){
            PDULobbyReq PDULobbyReq = new PDULobbyReq();
            PDULobbyReq.setActionFlag((byte) 1);
            PDULobbyReq.setLobbyID(lobbyID);
            gameClient.sendUnicast(PDUType.LOBBYREQUEST, PDULobbyReq);
        }

        /**
         * Attempts to leave current lobby.
         */
        public void leaveLobby(){
            PDULobbyReq PDULobbyRequest = new PDULobbyReq();
            PDULobbyRequest.setActionFlag((byte) 2);
            gameClient.sendUnicast(PDUType.LOBBYREQUEST, PDULobbyRequest);
        }

        public void requestLobbyList(){
            PDULobbyReq lobbyReq = new PDULobbyReq();
            lobbyReq.setActionFlag(PDULobbyReq.INFO);
            gameClient.sendUnicast(PDUType.LOBBYREQUEST, lobbyReq);
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
            gameClient.sendUnicast(PDUType.CHATMESSAGE, PDUChatMessage);
        }
    }

//    @Deprecated
//    public static class SimpleRTSGameClientGUI extends Application {
//        private Scene menuScene, gameSessionScene;
//        private GameSessionClientAPI gameClient;
//
//        @Override
//        public void start(Stage primaryStage) throws Exception {
//            primaryStage.setTitle("Simple RTS Game GUI Client");
//            FXMLLoader menuLoader = new FXMLLoader(Path.of("src/main/java/server/game/docker/client/ship/gui/fxml/menu.fxml").toUri().toURL());
//            FXMLLoader sessionLoader = new FXMLLoader(Path.of("src/main/java/server/game/docker/client/ship/gui/fxml/session.fxml").toUri().toURL());
//            menuLoader.setControllerFactory(type -> {
//                try {
//                    gameClient = new GameSessionClientAPI();
//                    return new MenuController(gameClient);
//    //                Stream.of(type.getFields()).filter(f -> f.getType().equals(type)).forEach(f -> {
//    //                    try {
//    //                        f.set(f, gameClient);
//    //                    } catch (IllegalAccessException e) {
//    //                        e.printStackTrace();
//    //                    }
//    //                });
//    //                return (Builder<?>) type.getConstructor().newInstance();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    //Game crash
//                    try {
//                        stop();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                    throw new RuntimeException(e);
//                }
//            });
//    //        sessionLoader.setBuilderFactory();
//            menuScene = new Scene(menuLoader.load());
//            gameSessionScene = new Scene(sessionLoader.load());
//            primaryStage.setScene(menuScene);
//            primaryStage.show();
//        }
//
//        @Override
//        public void stop() throws Exception {
//            super.stop();
//            gameClient.disconnect();
//            System.out.println("Exited client ...");
//        }
//
//        public static void main(String[] args) {
//            launch(args);
//        }
//
//        @Deprecated
//        static
//        class GameCLIClientLauncher {
//            public static void main(String[] args) {
//                new SimpleRTSGameClientCLI(args);
//            }
//        }
//
//        @Deprecated
//        public static class MenuController {
//            /*--------DI--------*/
//            @FXML
//            public Label lbl_lobby_info;
//            @FXML
//            public Label lbl_info_con_stat;
//            @FXML
//            public Label lbl_info_client_id;
//            @FXML
//            public BorderPane bp_main;
//            @FXML
//            public HBox hb_lobby_ui;
//            @FXML
//            public Button btn_create_lobby;
//            @FXML
//            public StackPane sp_main_content;
//            @FXML
//            public ScrollPane sp_lobby_list;
//            @FXML
//            public VBox vb_lobby_list;
//            /*--------fields--------*/
//            private final GameSessionClientAPI gameClient;
//            public Rectangle rtg_lobby_info_p1;
//            public Rectangle rtg_lobby_info_p2;
//            private VBox vb_chat;
//            private StackPane sp_player1;
//            private StackPane sp_player2;
//            private ScrollPane sp_chat;
//
//            public MenuController(GameSessionClientAPI gameClient) {
//                this.gameClient = gameClient;
//            }
//
//            public void initialize(){
//                sp_player1 = new StackPane();
//                sp_player2 = new StackPane();
//                final VBox vb_chat_outer = new VBox();
//                vb_chat = new VBox();
//                ScrollPane chatSP = new ScrollPane();
//                HBox chatInputHBox = new HBox();
//                TextField chatInputField = new TextField();
//                Button btnSend = new Button("Send");
//                btnSend.setStyle("-fx-background-color: #e1e123; -fx-text-fill: black;");
//                btnSend.setOnMouseClicked(evt -> Platform.runLater(() -> {
//                    gameClient.sendChatMessage(chatInputField.getText());
//                    addMessageToChat(chatInputField.getText(), "You");
//                    chatInputField.clear();
//                }));
//                chatInputHBox.getChildren().add(chatInputField);
//                chatInputHBox.getChildren().add(btnSend);
//                chatSP.setFitToHeight(true);
//                chatSP.setFitToWidth(true);
//                sp_chat = chatSP;
//                sp_chat.setMaxWidth(200);
//                sp_chat.setMinWidth(200);
//                sp_chat.setMaxHeight(150);
//                sp_chat.setMinHeight(150);
//                sp_chat.setFitToWidth(true);
//                vb_chat_outer.getChildren().add(sp_chat);
//                vb_chat_outer.getChildren().add(chatInputHBox);
//                vb_chat_outer.setMaxSize(200, 150);
//                chatSP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
//                sp_chat.setContent(vb_chat);
//                vb_chat.heightProperty().addListener((obs, oldVal, newVal) -> sp_chat.setVvalue((double) newVal));
//                sp_main_content.getChildren().add(vb_chat_outer);
//                StackPane.setAlignment(vb_chat_outer, Pos.BOTTOM_RIGHT);
//                vb_chat.setVisible(false);
//
//                rtg_lobby_info_p1 = new Rectangle(30, 30);
//                rtg_lobby_info_p1.setFill(Color.DARKGRAY); //todo: profile pic of player
//                sp_player1.getChildren().addAll(rtg_lobby_info_p1);
//                sp_player1.setAlignment(Pos.CENTER);
//
//                rtg_lobby_info_p2 = new Rectangle(30, 30);
//                rtg_lobby_info_p2.setFill(Color.DARKGRAY);
//                sp_player2.getChildren().addAll(rtg_lobby_info_p2);
//                sp_player2.setAlignment(Pos.CENTER);
//                hb_lobby_ui.getChildren().addAll(sp_player1, sp_player2);
//
//                sp_lobby_list.heightProperty().addListener((observable, oldValue, newValue) -> sp_lobby_list.setVvalue(newValue.doubleValue()));
//                //todo: only in GameClientHandler and through API IoC ... send -> joinLobby(), setOnJoinLobby() ... | examples -> game logic ... | user action handler, game logic handler (or maybe allow PDU registration?) and inject into GameClientHandler with PDUHandler
//        //        gameClient
//
//                new Thread(() -> {
//                    while(!gameClient.isConnected()){
//                        try {
//                            gameClient.connect();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            try {
//                                Thread.sleep(5000);
//                            } catch (InterruptedException ex) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }).start();
//
//                btn_create_lobby.setOnMouseClicked((MouseEvent t) -> gameClient.createLobby());
//
//                gameClient.setOnIDReceived(res -> Platform.runLater(() -> {
//                    lbl_info_con_stat.setText("Connected");
//                    lbl_info_con_stat.setStyle("-fx-text-fill: #e1e123;");
//                    lbl_info_client_id.setText(String.format("ID: %d", res.getNewClientID()));
//                    gameClient.requestLobbyList();
//                }));
//
//                gameClient.setOnLobbyCreate(res -> Platform.runLater(() -> {
//                    btn_create_lobby.setVisible(false);
//                    Button leaveLobbyBtn = new Button("Leave lobby");
//                    leaveLobbyBtn.setOnMouseClicked(event -> gameClient.leaveLobby());
//                    leaveLobbyBtn.setStyle("-fx-text-fill: white; -fx-background-color: #ff3333;");
//                    hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
//                    lbl_lobby_info.setText(String.format("Lobby %d:", res.getLobbyId()));
//
//                    rtg_lobby_info_p1.setFill(Color.LIGHTGREEN); //todo: profile pic of player
//                    sp_player1.setAlignment(Pos.CENTER);
//
//                    vb_chat.setVisible(true);
//                }));
//
//                gameClient.setOnLobbyJoined(res -> Platform.runLater(() -> {
//                    if(hb_lobby_ui.getChildren().size() == 4) {
//                        Button leaveLobbyBtn = new Button("Leave lobby");
//                        leaveLobbyBtn.setStyle("-fx-background-color: #ff3333; -fx-text-fill: white;");
//                        leaveLobbyBtn.setOnMouseClicked((MouseEvent event) -> gameClient.leaveLobby());
//                        hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
//                    }
//                    btn_create_lobby.setVisible(false);
//                    lbl_lobby_info.setText(String.format("Lobby %d:", res.getLobbyId()));
//                    rtg_lobby_info_p1.setFill(Color.LIGHTGREEN);
//                    rtg_lobby_info_p2.setFill(Color.LIGHTYELLOW);
//                    vb_chat.setVisible(true);
//                }));
//
//                gameClient.setOnLobbyLeave(r -> Platform.runLater(() -> {
//                    if(hb_lobby_ui.getChildren().size() == 5)
//                        hb_lobby_ui.getChildren().remove(1);
//                    rtg_lobby_info_p1.setFill(Color.DARKGRAY);
//                    rtg_lobby_info_p2.setFill(Color.DARKGRAY);
//                    lbl_lobby_info.setText("Lobby: ");
//                    btn_create_lobby.setVisible(true);
//                    vb_chat.setVisible(false);
//                    vb_chat.getChildren().clear();
//                }));
//
//                gameClient.setOnMemberJoined(res -> Platform.runLater(() -> {
//                    rtg_lobby_info_p2.setFill(Color.LIGHTYELLOW);
//                }));
//
//                gameClient.setOnMemberLeft(res -> Platform.runLater(() -> {
//                    rtg_lobby_info_p2.setFill(Color.DARKGRAY);
//                }));
//
//                gameClient.setOnLobbyBeacon(res -> addLobbyToList(res.getLobbyID(), res.getLobbyCurOccupancy(), res.getLobbyMaxOccupancy(), res.getLobbyListRefresh()));
//
//                gameClient.setOnLobbyChatMessageReceived(res -> Platform.runLater(() -> {addMessageToChat(res.getMessage(), res.getAuthorName());}));
//            }
//
//            private void addLobbyToList(Long lobbyID, Byte curOcc, Byte maxOcc, Boolean refreshList){
//                Platform.runLater(() -> {
//                    if(refreshList)
//                        vb_lobby_list.getChildren().clear();
//                    if(lobbyID.byteValue() == curOcc && curOcc.equals(maxOcc) && maxOcc == -1)
//                        return;
//                   HBox lobby = new HBox();
//                    Label lobbyIDLabel = new Label(String.format("Lobby: %d %d/%d", lobbyID, curOcc, maxOcc));
//                    lobbyIDLabel.setStyle("-fx-text-fill: white;");
//                    lobbyIDLabel.setPadding(new Insets(10, 20, 10, 40));
//                   Button btnJoin = new Button("Join");
//                    btnJoin.setOnMouseClicked(t -> gameClient.joinLobby(lobbyID));
//                   lobby.getChildren().addAll(lobbyIDLabel, btnJoin);
//                   lobby.setAlignment(Pos.CENTER);
//                   vb_lobby_list.getChildren().add(lobby);
//                });
//            }
//
//            private void addMessageToChat(String message, String senderName){
//                Platform.runLater(() -> {
//                    HBox hBox = new HBox();
//                    hBox.setAlignment(Pos.CENTER_LEFT);
//                    hBox.setPadding(new Insets(5, 5, 5, 10));
//
//                    Text text = new Text(String.format("[%s]: %s", senderName, message));
//                    text.setFill(Color.WHITE);
//                    TextFlow textFlow = new TextFlow(text);
//                    textFlow.setPadding(new Insets(5, 10, 5, 10));
//                    hBox.getChildren().addAll(textFlow);
//                    vb_chat.getChildren().add(hBox);
//                });
//            }
//        }
//
//        @Deprecated
//        public static class SessionController {
//            @FXML
//            public TextField tf_msg;
//            @FXML
//            public ScrollPane sp_chat;
//            @FXML
//            public VBox vb_msg;
//            @FXML
//            public Label lbl_info_con_stat;
//            @FXML
//            public Label lbl_info_sess_id;
//            @FXML
//            public Label lbl_info_client_id;
//            @FXML
//            public GridPane game_map;
//            @FXML
//            public Label hud_gold_count;
//            @FXML
//            public Label hud_gold_per_second;
//            @FXML
//            private Button btn_send_chat_msg;
//
//            static DatagramSocket socket;
//            private static InetAddress serverIP;
//            private static int serverPort;
//            private GameServerInitializer.RouterHandler actionPDUHandlerLegacy;
//            static Long clientID;
//            static Boolean listen;
//
//            @FXML
//            public void initialize() {
//                game_map.setPadding(new Insets(10, 10, 10, 10));
//                game_map.setHgap(10);
//                game_map.setVgap(8);
//                game_map.setStyle("-fx-background-color: #83c287");
//                sp_chat.setStyle("-fx-border-color: black; -fx-border-style: solid; -fx-focus-color: transparent;");
//        //        Rectangle rectangle = new Rectangle(20, 20, Paint.valueOf(Color.GRAY.toString()));
//        //        game_map.add(rectangle, 0, 0);
//        //        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 1, 0);
//        //        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 2, 0);
//        //        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 1, 1);
//        //        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 0, 1);
//        //        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 0, 2);
//        //        rectangle.setOnMouseClicked((MouseEvent t) -> System.out.println("Square clicked!"));
//                initMap();
//        //        try{
//        //            socket = new DatagramSocket();
//        //            socket.setSoTimeout(1000);
//        //            serverIP = InetAddress.getByName("127.0.0.1");
//        //            serverPort = 4321;
//        //            listen = true;
//        //        } catch (SocketException | UnknownHostException e) {
//        //            e.printStackTrace();
//        //        }
//
//                actionPDUHandlerLegacy = new GameServerInitializer.RouterHandler();
//
//                vb_msg.heightProperty().addListener((obs, oldV, newV) -> sp_chat.setVvalue((Double) newV));
//
//        //        actionHandler //todo: delegate to SimpleRTSGameClientSideLogic class that will inject actionMapper
//        //                .withMapping(PDUType.CHATMESSAGE.getID(), p -> {
//        //            vb_msg
//        //            addLabel(p.decode().get(1), vb_msg);
//        //                    appendChatMessage(p.decode().get(1), p.decode().get(0));
//        //                })
//        //                .withMapping(PDUType.INVALID.getID(), p -> System.out.println("Invalid packet received"))
//        //                .withMapping(PDUType.JOIN.getID(), p -> {
//                            //A second player has joined, game must be starting by now
//        //                    sendUnicast(new PDUBody(PDUType.GAMESTART.getID()));
//                            //todo: speacial MyPDU about successfull connection to a game session
//        //                    System.out.println("Game started");
//        //                    appendChatMessage("System", p.decode().get(0) + " has joined the session");
//        //                })
//        //                .withMapping(PDUType.IDREQUEST.getID(), p -> {
//        //                    clientID = Long.valueOf(p.decode().get(0));
//        //                    Platform.runLater(() -> {
//        //                        lbl_info_client_id.setText("Client ID: " + clientID);
//        //                        lbl_info_sess_id.setText("Session ID: " + p.decode().get(1));
//        //                        lbl_info_con_stat.setText("Connected");
//        //                    });
//        //                    appendChatMessage("System", "Connected to session");
//        //                    appendChatMessage("Waiting for other player ..."); todo: server manages
//        //                })
//        //                .withMapping(PDUType.DISCONNECT.getID(), p -> {
//        //                    appendChatMessage("System", "Client disconnected, game is forfeit");
//        //                    listen = false;
//        //                    System.exit(0);
//        //                })
//        //                .withMapping(PDUType.WORLDINFO.getID(), p -> {
//        //                    byte i = Byte.parseByte(p.decode().get(0)),
//        //                            j = Byte.parseByte(p.decode().get(1));
//        //                    TileType tileType = Stream.of(TileType.values()).filter(t -> t.getTileID().equals(Byte.parseByte(p.decode().get(2)))).findAny().orElse(TileType.BLANK);
//        //                    Platform.runLater(() -> {
//        //                        Rectangle gameTile = (Rectangle) game_map.getChildren().stream().filter(n -> GridPane.getRowIndex(n) == i && GridPane.getColumnIndex(n) == j).findAny().orElse(null);
//        //                        if(gameTile == null)
//        //                            return;
//        //                        switch(tileType) {
//        //                            case RESOURCENODE -> {
//        //                                gameTile.setFill(Color.YELLOW);
//        //                                gameTile.setOnMouseClicked((MouseEvent t) -> sendUnicast(PDUBody.fromPlayerMoveData(clientID, i, j, (byte) 0)));
//        //                            }
//        //                            case NEXUS, NEXUS2 -> gameTile.setFill(Color.DARKSLATEGRAY); //todo: maybe map rotation?
//        //                            case GOLDMINE -> {
//        //                                gameTile.setFill(Color.BLUEVIOLET);
//        //                                gameTile.setOnMouseClicked(null);
//        //                            }
//        //                        }
//        //                    });
//        //                })
//        //                .withMapping(PDUType.GAMESTART.getID(), p -> appendChatMessage("System", "Game has started"))
//        //                .withMapping(PDUType.GAMEEND.getID(), p -> {
//        //                    appendChatMessage("System", "Game has ended");
//        //                    appendChatMessage("System", "Exiting ...");
//        //                    listen = false;
//        //                    try {
//        //                        Thread.sleep(4000);
//        //                    } catch (InterruptedException e) {
//        //                        e.printStackTrace();
//        //                    }
//        //                    System.exit(0);
//        //                })
//        //                .withMapping(PDUType.SERVERTICKUPDATE.getID(), p -> Platform.runLater(() -> {
//        //                    hud_gold_count.setText(String.format("Gold: %s", p.decode().get(0)));
//        //                    hud_gold_per_second.setText(String.format("Gold/s: %s", p.decode().get(1)));
//        //                }));
//
//        //        new Thread(() -> {
//        //            while(listen){
//        //                byte [] data = new byte[1024];
//        //                DatagramPacket packet = new DatagramPacket(data, data.length);
//        //                try {
//                            //Attempt to reconnect until a ClientID is obtained
//        //                    if(clientID == null)
//        //                        sendUnicast(new PDUBody(PDUType.JOIN.getID()));
//        //                    socket.receive(packet);
//        //                    actionHandler.map(packet);
//        //                }
//        //                catch (SocketTimeoutException e) {
//        //                    e.printStackTrace();
//        //                }
//        //                catch (IOException e) {
//        //                    e.printStackTrace();
//        //                    break;
//        //                } catch (Exception e) {
//        //                    e.printStackTrace();
//        //                }
//        //            }
//        //            socket.close();
//        //        }).start();
//
//                btn_send_chat_msg.setOnAction(e -> {
//                    if(tf_msg.getText().isBlank() || clientID == null)
//                        return;
//        //            sendUnicast(new PDUBody(PDUType.CHATMESSAGE.getID(), clientID.toString(), tf_msg.getText()));
//                    HBox hBox = new HBox();
//                    hBox.setAlignment(Pos.CENTER_LEFT);
//                    hBox.setPadding(new Insets(5, 5, 5, 10));
//
//                    Text text = new Text(String.format("[You]: %s", tf_msg.getText()));
//                    TextFlow textFlow = new TextFlow(text);
//
//        //            textFlow.setStyle("-fx-color: rgb(239, 242, 255); -fx-background-color: rgb(15, 125, 242); -fx-background-radius: 20px");
//
//                    textFlow.setPadding(new Insets(5, 10, 5, 10));
//        //            text.setFill(Color.color(0.934, 0.945, 0.966));
//
//                    hBox.getChildren().addAll(textFlow);
//                    vb_msg.getChildren().add(hBox);
//                    tf_msg.clear();
//                });
//
//        //        lbl_info.setOnAction(e -> sendUnicast(new MyPDU00Join()));
//            }
//
//            public static void sendUnicast(PDU p){
//        //        DatagramPacket packet = new DatagramPacket(p.getByteBuffer(), p.getByteBuffer().length, serverIP, serverPort);
//        //        try {
//        //            socket.send(packet);
//        //        } catch (IOException e) {
//        //            e.printStackTrace();
//        //        }
//            }
//
//            private void appendChatMessage(String senderName, String msg){
//                Platform.runLater(() -> {
//                    HBox hBox = new HBox();
//                    hBox.setAlignment(Pos.CENTER_LEFT);
//                    hBox.setPadding(new Insets(5, 5, 5, 10));
//
//                    Text text = new Text(String.format("[%s]: %s", senderName, msg));
//                    TextFlow textFlow = new TextFlow(text);
//        //            textFlow.setStyle("-fx-background-color: rgb(233, 233, 235); -fx-background-radius: 20px");
//                    textFlow.setPadding(new Insets(5, 10, 5, 10));
//                    hBox.getChildren().addAll(textFlow);
//                    vb_msg.getChildren().add(hBox);
//                });
//            }
//
//            private void initMap(){
//                for (byte i = 0; i < 11; i++)
//                    for (byte j = 0; j < 11; j++) {
//                        //Water
//                        if(j == 5) {
//        //                    Rectangle waterTile = new Rectangle(40, 40, Color.valueOf("#006994"));
//        //                    final byte iFin = i, jFin = j;
//        //                    waterTile.setOnMouseClicked((MouseEvent t) -> sendUnicast(PDUBody.fromPlayerMoveData(clientID, iFin, jFin, (byte) 1)));
//        //                    game_map.add(waterTile, i, j);
//
//                            continue;
//                        }
//                        //Basic tile
//                        game_map.add(new Rectangle(40, 40, Color.TRANSPARENT), i, j);
//                    }
//            }
//
//        //    public static void addLabel(String messageFromServer, VBox vBox){
//        //        HBox hBox = new HBox();
//        //        hBox.setAlignment(Pos.CENTER_LEFT);
//        //        hBox.setPadding(new Insets(5, 5, 5, 10));
//        //
//        //        Text text = new Text(messageFromServer);
//        //        TextFlow textFlow = new TextFlow(text);
//        //        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235); -fx-background-radius: 20px");
//        //        textFlow.setPadding(new Insets(5, 10, 5, 10));
//        //        hBox.getChildren().addAll(textFlow);
//        //    }
//        }
//
//        @Deprecated
//        public static class SimpleRTSGameClientCLI {
//            //Config
//            /**
//             * The server port
//             */
//            public final Integer serverPort;
//            /**
//             * The server IP address
//             */
//            private InetAddress iPAddress;
//            /**
//             * The client socket
//             */
//            private DatagramSocket socket;
//
//            public InetAddress getiPAddress() {
//                return iPAddress;
//            }
//            public static final String ANSI_RESET = "\u001B[0m";
//            public static final String ANSI_BLACK = "\u001B[30m";
//            public static final String ANSI_RED = "\u001B[31m";
//            public static final String ANSI_GREEN = "\u001B[32m";
//            public static final String ANSI_YELLOW = "\u001B[33m";
//            public static final String ANSI_BLUE = "\u001B[34m";
//            public static final String ANSI_PURPLE = "\u001B[35m";
//            public static final String ANSI_CYAN = "\u001B[36m";
//            public static final String ANSI_WHITE = "\u001B[37m";
//
//            private final byte [][] gameMap;
//            private final Thread clientNetworkListener;
//            private Long clientID;
//        //    private final Handler actionRouter;
//            private String player1Name;
//            private String player2Name;
//
//            public SimpleRTSGameClientCLI(String [] args){
//                serverPort = 4321;
//                gameMap = new byte[][] {
//                    {0, 0, 0, 0, 0, Byte.MAX_VALUE, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                    {0, 0, 0, 0, 0, Byte.MAX_VALUE, 0, 0, 0, 0, 0}
//                };
//                try {
//                    socket = new DatagramSocket();
//                    iPAddress = InetAddress.getByName("localhost");
//                } catch (UnknownHostException | SocketException e) {
//                    e.printStackTrace();
//                }
//        //        actionRouter = new Handler()
//        //            .withMapping(PDUType.INVALID.getID(), p -> System.out.println("Invalid packet received"))
//        //            .withMapping(PDUType.JOIN.getID(), p -> {
//                        //A second player has joined, game must be starting by now
//        //                sendUnicast(new PDUBody((PDUType.IDREQUEST.getID())));
//                        //todo: speacial MyPDU about successfull connection to a game session
//        //                System.out.println("Game started");
//        //            })
//        //            .withMapping(PDUType.DISCONNECT.getID(), p -> {
//        //                System.out.println("A player has forfeit, game is over");
//        //                System.exit(0);
//        //            })
//        //            .withMapping(PDUType.WORLDINFO.getID(), p -> {
//        //                Vector<String> packetData = p.decode();
//        //                byte
//        //                    i = Byte.parseByte(packetData.get(0)),
//        //                    j = Byte.parseByte(packetData.get(1)),
//        //                    tileID = Byte.parseByte(packetData.get(2));
//        //                gameMap[i][j] = tileID;
//        //                drawMap();
//        //            })
//        //            .withMapping((byte) 5, p -> clientID = Long.valueOf(p.decode().get(0)))
//        //            .withMapping((byte) 6, p -> {
//        //                System.out.println(String.format("%s!", Long.parseLong(p.decode().get(0)) == clientID? "Victory" : "Defeat"));
//        //                System.exit(0);
//        //            })
//        //            .withMapping((byte) 7, p -> System.out.println(String.format("Gold update: %s", p.decode().get(0))));
//                clientNetworkListener = new Thread(){
//                    @Override
//                    public void run() {
//                        while(true){
//                            byte [] data = new byte[1024];
//                            DatagramPacket packet = new DatagramPacket(data, data.length);
//                            try {
//                                socket.receive(packet);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//        //                    actionRouter.map(packet);
//                        }
//                    }
//                };
//                clientNetworkListener.start();
//                //Give control to player
//                initInputLoop();
//            }
//
//            private void drawMap(){
//                //Draw map
//                for (int i = 0; i < gameMap.length; i++) {
//                    for (int j = 0; j < gameMap[i].length; j++) {
//                        if(gameMap[i][j] == GameSessionHandler.TileType.NEXUS.getTileID())
//                            System.out.printf("  %sN1%s  ", ANSI_GREEN, ANSI_RESET);
//                        else if(gameMap[i][j] == GameSessionHandler.TileType.NEXUS2.getTileID())
//                            System.out.printf("  %sN2%s  ", ANSI_GREEN, ANSI_RESET);
//                        else if(gameMap[i][j] == GameSessionHandler.TileType.RESOURCENODE.getTileID())
//                            System.out.printf("  %sG(a)%s  ", ANSI_YELLOW, ANSI_RESET);
//                        else if(gameMap[i][j] == GameSessionHandler.TileType.GOLDMINE.getTileID())
//                            System.out.printf("  %sM()%s  ", ANSI_PURPLE, ANSI_RESET);
//                        else if(gameMap[i][j] == GameSessionHandler.TileType.RIVER.getTileID())
//                            System.out.printf("  %s====%s  ", ANSI_BLUE, ANSI_RESET);
//                        else
//                            System.out.printf("  %4d  ", gameMap[i][j]);
//                    }
//                    System.out.println();
//                }
//            }
//
//            private void initInputLoop(){
//                System.out.println("Type 'join' to join a session");
//                while(true){
//                    switch(awaitPlayerInput()){
//                        case "join" -> {
//                            System.out.println("Joining a server ...");
//        //                    sendUnicast(new PDUBody(PDUType.JOIN.getID()));
//                        }
//        //                case "exit" ->
//        //                    sendUnicast(new PDUBody().fromDisconnectData(clientID));
//                        case "mine" ->
//                            buyGoldMine();
//        //                case "bridge" ->
//        //                    sendUnicast(new PDUBody(PDUType.PLAYERMOVE.getID(), clientID.toString(), "1"));
//                        default ->
//                            System.out.println("Unrecognised command");
//                    }
//                }
//            }
//
//            private String awaitPlayerInput(){
//                try /*(BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in)))*/ {
//                    BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));
//                    return playerInput.readLine();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//
//            public void sendUnicast(PDU p){
//        //        DatagramPacket packet = new DatagramPacket(p.getByteBuffer(), p.getByteBuffer().length, iPAddress, serverPort);
//        //        try {
//        //            socket.send(packet);
//        //        } catch (IOException e) {
//        //            e.printStackTrace();
//        //        }
//            }
//
//            public void buyGoldMine(){
//        //        sendUnicast(new PDUBody(PDUType.PLAYERMOVE.getID(), clientID.toString(), "0"));
//            }
//        }
//    }
}
