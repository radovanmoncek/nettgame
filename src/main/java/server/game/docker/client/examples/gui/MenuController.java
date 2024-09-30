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
import server.game.docker.client.GameClient;
import server.game.docker.net.dto.IDRes;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;


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
    public Rectangle rtg_lobby_info_p1;
    public Rectangle rtg_lobby_info_p2;
    @FXML
    public Button btn_create_lobby;
    @FXML
    public StackPane sp_main_content;
    @FXML
    public ScrollPane sp_lobby_list;
    @FXML
    public VBox vb_lobby_list;
    private final GameClient gameClient;
    /*--------fields--------*/
    private VBox chat;
    private StackPane sPP1;
    private StackPane sPP2;

    public MenuController(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public void initialize(){
        sPP1 = new StackPane();
        sPP2 = new StackPane();
        rtg_lobby_info_p1 = new Rectangle(30, 30);
        rtg_lobby_info_p1.setFill(Color.DARKGRAY); //todo: profile pic of player
        sPP1.getChildren().addAll(rtg_lobby_info_p1);
        sPP1.setAlignment(Pos.CENTER);

        rtg_lobby_info_p2 = new Rectangle(30, 30);
        rtg_lobby_info_p2.setFill(Color.DARKGRAY);
        sPP2.getChildren().addAll(rtg_lobby_info_p2);
        sPP2.setAlignment(Pos.CENTER);
        hb_lobby_ui.getChildren().addAll(sPP1, sPP2);

        sp_lobby_list.heightProperty().addListener((observable, oldValue, newValue) -> sp_lobby_list.setVvalue(newValue.doubleValue()));
        //todo: only in GameClientHandler and through API IoC ... send -> joinLobby(), setOnJoinLobby() ... | examples -> game logic ... | user action handler, game logic handler (or maybe allow PDU registration?) and inject into GameClientHandler with PDUHandler
//        gameClient

        new Thread(() -> {
            while(!gameClient.isConnected()){
                try {
                    gameClient.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        btn_create_lobby.setOnMouseClicked((MouseEvent t) -> gameClient.createLobby());

        gameClient.setOnIDReceived(res -> Platform.runLater(() -> {
            lbl_info_con_stat.setText("Connected");
            lbl_info_con_stat.setStyle("-fx-text-fill: green;");
            lbl_info_client_id.setText(String.format("ID: %d", res.getNewClientID()));
        }));

        gameClient.setOnLobbyCreate(res -> Platform.runLater(() -> {
            btn_create_lobby.setVisible(false);
            Button leaveLobbyBtn = new Button("Leave lobby");
            leaveLobbyBtn.setOnMouseClicked(event -> gameClient.leaveLobby());
            hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
            lbl_lobby_info.setText(String.format("Lobby %d:", res.getLobbyId()));

            rtg_lobby_info_p1.setFill(Color.LIGHTGREEN); //todo: profile pic of player
            Text text = new Text("P1");
            text.setFill(Color.WHITE);
            sPP1.getChildren().addAll(text);
            sPP1.setAlignment(Pos.CENTER);

            chat = new VBox();
            ScrollPane chatSP = new ScrollPane();
            HBox chatInputHBox = new HBox();
            TextField chatInputField = new TextField();
            Button btnSend = new Button("Send");
            chatInputHBox.getChildren().add(chatInputField);
            chatInputHBox.getChildren().add(btnSend);
            chatSP.setFitToHeight(true);
            chatSP.setFitToWidth(true);
            chat.getChildren().add(chatSP);
            chat.getChildren().add(chatInputHBox);
            chat.setMaxSize(200, 150);
            chatSP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            sp_main_content.getChildren().add(chat);
            StackPane.setAlignment(chat, Pos.BOTTOM_RIGHT);
        }));

        gameClient.setOnLobbyJoined(res -> Platform.runLater(() -> {
            if (!res.getLobbyID().equals(-1L)) {
                btn_create_lobby.setVisible(false);
                Button leaveLobbyBtn = new Button("Leave lobby");
                leaveLobbyBtn.setOnMouseClicked((MouseEvent event) -> gameClient.leaveLobby());
                hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
                lbl_lobby_info.setText(String.format("Lobby %d:", res.getLobbyID()));
            }

            rtg_lobby_info_p1.setFill(Color.LIGHTGREEN);
            Text text1 = new Text("P1");
            text1.setFill(Color.WHITE);
            sPP1.getChildren().addAll(text1);
            rtg_lobby_info_p2.setFill(Color.LIGHTGREEN);
            Text text2 = new Text("P2");
            text2.setFill(Color.WHITE);
            sPP2.getChildren().addAll(text2);
        }));

        gameClient.setOnLobbyLeave(r -> Platform.runLater(() -> {
            if (!r.isLeader()) {
                hb_lobby_ui.getChildren().remove(1);
                rtg_lobby_info_p1.setFill(Color.DARKGRAY);
                sPP1.getChildren().remove(1);
                lbl_lobby_info.setText("Lobby:");
                btn_create_lobby.setVisible(true);
                chat.setVisible(false);
            }

            rtg_lobby_info_p2.setFill(Color.DARKGRAY);
            //Other player didn't have to be connected
            if (sPP2.getChildren().size() > 1)
                sPP2.getChildren().remove(1);
        }));

        gameClient.setOnLobbyBeacon(res -> addLobbyToList(res.getLobbyID(), res.getLobbyCurOccupancy(), res.getLobbyMaxOccupancy(), res.getLobbyListRefresh()));
    }

    private void addLobbyToList(Long lobbyID, Byte curOcc, Byte maxOcc, Boolean refreshList){
        Platform.runLater(() -> {
            if(refreshList)
                vb_lobby_list.getChildren().clear();
            //Lobby list is empty - "transitive" -1
            if(lobbyID.byteValue() == curOcc && curOcc.equals(maxOcc) && maxOcc == -1)
                return;
           HBox lobby = new HBox();
            Label lobbyIDLabel = new Label(String.format("Lobby: %d %d/%d", lobbyID, curOcc, maxOcc));
            lobbyIDLabel.setStyle("-fx-text-fill: black;");
            lobbyIDLabel.setPadding(new Insets(10, 20, 10, 40));
           Button btnJoin = new Button("Join");
            btnJoin.setOnMouseClicked(t -> gameClient.joinLobby(lobbyID));
           lobby.getChildren().addAll(lobbyIDLabel, btnJoin);
           lobby.setAlignment(Pos.CENTER);
           vb_lobby_list.getChildren().add(lobby);
        });
    }
}
