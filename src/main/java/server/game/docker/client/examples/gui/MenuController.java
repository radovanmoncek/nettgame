package server.game.docker.client.examples.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import server.game.docker.client.GameSessionClientAPI;


public class MenuController {
    /*--------DI--------*/
    @FXML
    public Label lbl_lobby_info;
    @FXML
    public Label lbl_info_con_stat;
    @FXML
    public Label lbl_info_client_id;
    @FXML
    public BorderPane bp_main;
    @FXML
    public HBox hb_lobby_ui;
    @FXML
    public Button btn_create_lobby;
    @FXML
    public StackPane sp_main_content;
    @FXML
    public ScrollPane sp_lobby_list;
    @FXML
    public VBox vb_lobby_list;
    /*--------fields--------*/
    private final GameSessionClientAPI gameClient;
    public Rectangle rtg_lobby_info_p1;
    public Rectangle rtg_lobby_info_p2;
    private VBox vb_chat;
    private StackPane sp_player1;
    private StackPane sp_player2;
    private ScrollPane sp_chat;

    public MenuController(GameSessionClientAPI gameClient) {
        this.gameClient = gameClient;
    }

    public void initialize(){
        sp_player1 = new StackPane();
        sp_player2 = new StackPane();
        final VBox vb_chat_outer = new VBox();
        vb_chat = new VBox();
        ScrollPane chatSP = new ScrollPane();
        HBox chatInputHBox = new HBox();
        TextField chatInputField = new TextField();
        Button btnSend = new Button("Send");
        btnSend.setStyle("-fx-background-color: #e1e123; -fx-text-fill: black;");
        btnSend.setOnMouseClicked(evt -> Platform.runLater(() -> {
            gameClient.sendChatMessage(chatInputField.getText());
            addMessageToChat(chatInputField.getText(), "You");
            chatInputField.clear();
        }));
        chatInputHBox.getChildren().add(chatInputField);
        chatInputHBox.getChildren().add(btnSend);
        chatSP.setFitToHeight(true);
        chatSP.setFitToWidth(true);
        sp_chat = chatSP;
        sp_chat.setMaxWidth(200);
        sp_chat.setMinWidth(200);
        sp_chat.setMaxHeight(150);
        sp_chat.setMinHeight(150);
        sp_chat.setFitToWidth(true);
        vb_chat_outer.getChildren().add(sp_chat);
        vb_chat_outer.getChildren().add(chatInputHBox);
        vb_chat_outer.setMaxSize(200, 150);
        chatSP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        sp_chat.setContent(vb_chat);
        vb_chat.heightProperty().addListener((obs, oldVal, newVal) -> sp_chat.setVvalue((double) newVal));
        sp_main_content.getChildren().add(vb_chat_outer);
        StackPane.setAlignment(vb_chat_outer, Pos.BOTTOM_RIGHT);
        vb_chat.setVisible(false);

        rtg_lobby_info_p1 = new Rectangle(30, 30);
        rtg_lobby_info_p1.setFill(Color.DARKGRAY); //todo: profile pic of player
        sp_player1.getChildren().addAll(rtg_lobby_info_p1);
        sp_player1.setAlignment(Pos.CENTER);

        rtg_lobby_info_p2 = new Rectangle(30, 30);
        rtg_lobby_info_p2.setFill(Color.DARKGRAY);
        sp_player2.getChildren().addAll(rtg_lobby_info_p2);
        sp_player2.setAlignment(Pos.CENTER);
        hb_lobby_ui.getChildren().addAll(sp_player1, sp_player2);

        sp_lobby_list.heightProperty().addListener((observable, oldValue, newValue) -> sp_lobby_list.setVvalue(newValue.doubleValue()));
        //todo: only in GameClientHandler and through API IoC ... send -> joinLobby(), setOnJoinLobby() ... | examples -> game logic ... | user action handler, game logic handler (or maybe allow PDU registration?) and inject into GameClientHandler with PDUHandler
//        gameClient

        new Thread(() -> {
            while(!gameClient.isConnected()){
                try {
                    gameClient.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        btn_create_lobby.setOnMouseClicked((MouseEvent t) -> gameClient.createLobby());

        gameClient.setOnIDReceived(res -> Platform.runLater(() -> {
            lbl_info_con_stat.setText("Connected");
            lbl_info_con_stat.setStyle("-fx-text-fill: #e1e123;");
            lbl_info_client_id.setText(String.format("ID: %d", res.getNewClientID()));
            gameClient.requestLobbyList();
        }));

        gameClient.setOnLobbyCreate(res -> Platform.runLater(() -> {
            btn_create_lobby.setVisible(false);
            Button leaveLobbyBtn = new Button("Leave lobby");
            leaveLobbyBtn.setOnMouseClicked(event -> gameClient.leaveLobby());
            leaveLobbyBtn.setStyle("-fx-text-fill: white; -fx-background-color: #ff3333;");
            hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
            lbl_lobby_info.setText(String.format("Lobby %d:", res.getLobbyId()));

            rtg_lobby_info_p1.setFill(Color.LIGHTGREEN); //todo: profile pic of player
            sp_player1.setAlignment(Pos.CENTER);

            vb_chat.setVisible(true);
        }));

        gameClient.setOnLobbyJoined(res -> Platform.runLater(() -> {
            if(hb_lobby_ui.getChildren().size() == 4) {
                Button leaveLobbyBtn = new Button("Leave lobby");
                leaveLobbyBtn.setStyle("-fx-background-color: #ff3333; -fx-text-fill: white;");
                leaveLobbyBtn.setOnMouseClicked((MouseEvent event) -> gameClient.leaveLobby());
                hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
            }
            btn_create_lobby.setVisible(false);
            lbl_lobby_info.setText(String.format("Lobby %d:", res.getLobbyId()));
            rtg_lobby_info_p1.setFill(Color.LIGHTGREEN);
            rtg_lobby_info_p2.setFill(Color.LIGHTYELLOW);
            vb_chat.setVisible(true);
        }));

        gameClient.setOnLobbyLeave(r -> Platform.runLater(() -> {
            if(hb_lobby_ui.getChildren().size() == 5)
                hb_lobby_ui.getChildren().remove(1);
            rtg_lobby_info_p1.setFill(Color.DARKGRAY);
            rtg_lobby_info_p2.setFill(Color.DARKGRAY);
            lbl_lobby_info.setText("Lobby: ");
            btn_create_lobby.setVisible(true);
            vb_chat.setVisible(false);
            vb_chat.getChildren().clear();
        }));

        gameClient.setOnMemberJoined(res -> Platform.runLater(() -> {
            rtg_lobby_info_p2.setFill(Color.LIGHTYELLOW);
        }));

        gameClient.setOnMemberLeft(res -> Platform.runLater(() -> {
            rtg_lobby_info_p2.setFill(Color.DARKGRAY);
        }));

        gameClient.setOnLobbyBeacon(res -> addLobbyToList(res.getLobbyID(), res.getLobbyCurOccupancy(), res.getLobbyMaxOccupancy(), res.getLobbyListRefresh()));

        gameClient.setOnLobbyChatMessageReceived(res -> Platform.runLater(() -> {addMessageToChat(res.getMessage(), res.getAuthorName());}));
    }

    private void addLobbyToList(Long lobbyID, Byte curOcc, Byte maxOcc, Boolean refreshList){
        Platform.runLater(() -> {
            if(refreshList)
                vb_lobby_list.getChildren().clear();
            if(lobbyID.byteValue() == curOcc && curOcc.equals(maxOcc) && maxOcc == -1)
                return;
           HBox lobby = new HBox();
            Label lobbyIDLabel = new Label(String.format("Lobby: %d %d/%d", lobbyID, curOcc, maxOcc));
            lobbyIDLabel.setStyle("-fx-text-fill: white;");
            lobbyIDLabel.setPadding(new Insets(10, 20, 10, 40));
           Button btnJoin = new Button("Join");
            btnJoin.setOnMouseClicked(t -> gameClient.joinLobby(lobbyID));
           lobby.getChildren().addAll(lobbyIDLabel, btnJoin);
           lobby.setAlignment(Pos.CENTER);
           vb_lobby_list.getChildren().add(lobby);
        });
    }

    private void addMessageToChat(String message, String senderName){
        Platform.runLater(() -> {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text text = new Text(String.format("[%s]: %s", senderName, message));
            text.setFill(Color.WHITE);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setPadding(new Insets(5, 10, 5, 10));
            hBox.getChildren().addAll(textFlow);
            vb_chat.getChildren().add(hBox);
        });
    }
}
