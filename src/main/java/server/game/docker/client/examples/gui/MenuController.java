package server.game.docker.client.examples.gui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import server.game.docker.net.*;
import server.game.docker.net.dto.*;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import static server.game.docker.client.examples.gui.SimpleRTSGameClientGUI.gameClient;

public class MenuController {
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
    private VBox chat;
    private StackPane sPP1;
    private StackPane sPP2;

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
        gameClient
                //Inbound only PDU
                .registerPDU(PDUType.IDRES, new LocalPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        IDRes out = new IDRes();
                        out.setNewClientID(in.readLong());
                        return out;
                    }

                    @Override
                    public ByteBuf encode(Object in) {
                        return null;
                    }

                    @Override
                    public void handle(PDU p) {
                        Platform.runLater(() -> {
                            IDRes iDRes = (IDRes) p.getData();
                            lbl_info_con_stat.setText("Connected");
                            lbl_info_con_stat.setStyle("-fx-text-fill: green;");
                            lbl_info_client_id.setText(String.format("ID: %d", iDRes.getNewClientID()));
                        });
                    }
                })
                //Outbound PDU with no payload
                .appendPipeline(PDUType.CREATELOBBYREQ, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {return Unpooled.buffer(0);}
                })
                //Inbound PDU with 32-bit Integer payload and required action
                .appendPipeline(PDUType.CREATELOBBYRES, new AbstractLocalInboundActionPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        CreateLobbyRes out = new CreateLobbyRes();
                        out.setLobbyId(in.readLong());
                        return out;
                    }

                    @Override
                    public void handle(PDU p) {
                        CreateLobbyRes createLobbyRes = (CreateLobbyRes) p.getData();
                        Platform.runLater(() -> {
                            btn_create_lobby.setVisible(false);
                            Button leaveLobbyBtn = new Button("Leave lobby");
                            leaveLobbyBtn.setOnMouseClicked(event -> gameClient.sendUnicast(new PDU(PDUType.LEAVELOBBYREQ, null, null, null)));
                            hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
                            lbl_lobby_info.setText(String.format("Lobby %d:", createLobbyRes.getLobbyId()));

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
                        });
                    }
                })
                //Outbound only PDU with 8B Long data and no action
                .appendPipeline(PDUType.JOINLOBBYREQ, new AbstractLocalOutboundPipeline() {
                    @Override
                    public ByteBuf encode(Object in) {
                        JoinLobbyReq joinLobbyReq = (JoinLobbyReq) in;
                        return Unpooled.buffer(Long.BYTES).writeLong(joinLobbyReq.getLobbyID());
                    }
                })
                //Inbound only PDU with 8B long data and action - a form of ack
                .appendPipeline(PDUType.JOINLOBBYRES, new AbstractLocalInboundActionPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        JoinLobbyRes out = new JoinLobbyRes();
                        out.setLobbyID(in.readLong());
                        return out;
                    }

                    @Override
                    public void handle(PDU p) {
                        JoinLobbyRes joinLobbyRes = (JoinLobbyRes) p.getData();
                        Platform.runLater(() -> {
                            if(!joinLobbyRes.getLobbyID().equals(-1L)) {
                                btn_create_lobby.setVisible(false);
                                Button leaveLobbyBtn = new Button("Leave lobby");
                                leaveLobbyBtn.setOnMouseClicked((MouseEvent event) -> gameClient.sendUnicast(new PDU(PDUType.LEAVELOBBYREQ, null, null, null)));
                                hb_lobby_ui.getChildren().add(1, leaveLobbyBtn);
                                lbl_lobby_info.setText(String.format("Lobby %d:", joinLobbyRes.getLobbyID()));
                            }

                            rtg_lobby_info_p1.setFill(Color.LIGHTGREEN);
                            Text text1 = new Text("P1");
                            text1.setFill(Color.WHITE);
                            sPP1.getChildren().addAll(text1);
                            rtg_lobby_info_p2.setFill(Color.LIGHTGREEN);
                            Text text2 = new Text("P2");
                            text2.setFill(Color.WHITE);
                            sPP2.getChildren().addAll(text2);
                        });
                    }
                })
                //Outbound PDU with no payload and no action (registered only)
                .appendPipeline(PDUType.LEAVELOBBYREQ, new DefaultLocalPipeline())
                //Inbound PDU with no payload and action
                .appendPipeline(PDUType.LEAVELOBBYRES, new AbstractLocalInboundActionPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        LeaveLobbyRes out = new LeaveLobbyRes();
                        out.setLeader(in.readBoolean());
                        return out;
                    }

                    @Override
                    public void handle(PDU p) {
                        LeaveLobbyRes leaveLobbyRes = (LeaveLobbyRes) p.getData();
                        Platform.runLater(() -> {
                            if(!leaveLobbyRes.isLeader()) {
                                hb_lobby_ui.getChildren().remove(1);
                                rtg_lobby_info_p1.setFill(Color.DARKGRAY);
                                sPP1.getChildren().remove(1);
                                lbl_lobby_info.setText("Lobby:");
                                btn_create_lobby.setVisible(true);
                                chat.setVisible(false);
                            }

                            rtg_lobby_info_p2.setFill(Color.DARKGRAY);
                            //Other player didn't have to be connected
                            if(sPP2.getChildren().size() > 1)
                                sPP2.getChildren().remove(1);
                        });
                    }
                })
                //Inbound only 8B Long + 2 * 1B Byte
                .appendPipeline(PDUType.LOBBYBEACON, new AbstractLocalInboundActionPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        LobbyBeacon out = new LobbyBeacon();
                        out.setLobbyID(in.readLong());
                        out.setLobbyCurOccupancy(in.readByte());
                        out.setLobbyMaxOccupancy(in.readByte());
                        out.setLobbyListRefresh(in.readBoolean());
                        return out;
                    }

                    @Override
                    public void handle(PDU p) {
                        LobbyBeacon lobbyBeacon = (LobbyBeacon) p.getData();
                        addLobbyToList(lobbyBeacon.getLobbyID(), lobbyBeacon.getLobbyCurOccupancy(), lobbyBeacon.getLobbyMaxOccupancy(), lobbyBeacon.getLobbyListRefresh());
                    }
                });
        new Thread(() -> {
            while(!gameClient.isConnected()){
                try {
                    gameClient.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        btn_create_lobby.setOnMouseClicked((MouseEvent t) -> gameClient.sendUnicast(new PDU(PDUType.CREATELOBBYREQ, null, null, null)));

//        gameClient.setOnLobbyLeave(r -> );
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
