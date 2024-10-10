package server.game.docker.client.examples.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import server.game.docker.net.pipelines.PDUMultiPipeline;
import server.game.docker.net.parents.pdus.PDU;

import java.net.*;

public class SessionController {
    @FXML
    public TextField tf_msg;
    @FXML
    public ScrollPane sp_chat;
    @FXML
    public VBox vb_msg;
    @FXML
    public Label lbl_info_con_stat;
    @FXML
    public Label lbl_info_sess_id;
    @FXML
    public Label lbl_info_client_id;
    @FXML
    public GridPane game_map;
    @FXML
    public Label hud_gold_count;
    @FXML
    public Label hud_gold_per_second;
    @FXML
    private Button btn_send_chat_msg;

    static DatagramSocket socket;
    private static InetAddress serverIP;
    private static int serverPort;
    private PDUMultiPipeline.PDUHandlerLegacy actionPDUHandlerLegacy;
    static Long clientID;
    static Boolean listen;

    @FXML
    public void initialize() {
        game_map.setPadding(new Insets(10, 10, 10, 10));
        game_map.setHgap(10);
        game_map.setVgap(8);
        game_map.setStyle("-fx-background-color: #83c287");
        sp_chat.setStyle("-fx-border-color: black; -fx-border-style: solid; -fx-focus-color: transparent;");
//        Rectangle rectangle = new Rectangle(20, 20, Paint.valueOf(Color.GRAY.toString()));
//        game_map.add(rectangle, 0, 0);
//        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 1, 0);
//        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 2, 0);
//        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 1, 1);
//        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 0, 1);
//        game_map.add(new Rectangle(20, 20, Paint.valueOf(Color.BLUE.toString())), 0, 2);
//        rectangle.setOnMouseClicked((MouseEvent t) -> System.out.println("Square clicked!"));
        initMap();
//        try{
//            socket = new DatagramSocket();
//            socket.setSoTimeout(1000);
//            serverIP = InetAddress.getByName("127.0.0.1");
//            serverPort = 4321;
//            listen = true;
//        } catch (SocketException | UnknownHostException e) {
//            e.printStackTrace();
//        }

        actionPDUHandlerLegacy = new PDUMultiPipeline.PDUHandlerLegacy();

        vb_msg.heightProperty().addListener((obs, oldV, newV) -> sp_chat.setVvalue((Double) newV));

//        actionHandler //todo: delegate to SimpleRTSGameClientSideLogic class that will inject actionMapper
//                .withMapping(PDUType.CHATMESSAGE.getID(), p -> {
//            vb_msg
//            addLabel(p.decode().get(1), vb_msg);
//                    appendChatMessage(p.decode().get(1), p.decode().get(0));
//                })
//                .withMapping(PDUType.INVALID.getID(), p -> System.out.println("Invalid packet received"))
//                .withMapping(PDUType.JOIN.getID(), p -> {
                    //A second player has joined, game must be starting by now
//                    sendUnicast(new PDUBody(PDUType.GAMESTART.getID()));
                    //todo: speacial MyPDU about successfull connection to a game session
//                    System.out.println("Game started");
//                    appendChatMessage("System", p.decode().get(0) + " has joined the session");
//                })
//                .withMapping(PDUType.IDREQUEST.getID(), p -> {
//                    clientID = Long.valueOf(p.decode().get(0));
//                    Platform.runLater(() -> {
//                        lbl_info_client_id.setText("Client ID: " + clientID);
//                        lbl_info_sess_id.setText("Session ID: " + p.decode().get(1));
//                        lbl_info_con_stat.setText("Connected");
//                    });
//                    appendChatMessage("System", "Connected to session");
//                    appendChatMessage("Waiting for other player ..."); todo: server manages
//                })
//                .withMapping(PDUType.DISCONNECT.getID(), p -> {
//                    appendChatMessage("System", "Client disconnected, game is forfeit");
//                    listen = false;
//                    System.exit(0);
//                })
//                .withMapping(PDUType.WORLDINFO.getID(), p -> {
//                    byte i = Byte.parseByte(p.decode().get(0)),
//                            j = Byte.parseByte(p.decode().get(1));
//                    TileType tileType = Stream.of(TileType.values()).filter(t -> t.getTileID().equals(Byte.parseByte(p.decode().get(2)))).findAny().orElse(TileType.BLANK);
//                    Platform.runLater(() -> {
//                        Rectangle gameTile = (Rectangle) game_map.getChildren().stream().filter(n -> GridPane.getRowIndex(n) == i && GridPane.getColumnIndex(n) == j).findAny().orElse(null);
//                        if(gameTile == null)
//                            return;
//                        switch(tileType) {
//                            case RESOURCENODE -> {
//                                gameTile.setFill(Color.YELLOW);
//                                gameTile.setOnMouseClicked((MouseEvent t) -> sendUnicast(PDUBody.fromPlayerMoveData(clientID, i, j, (byte) 0)));
//                            }
//                            case NEXUS, NEXUS2 -> gameTile.setFill(Color.DARKSLATEGRAY); //todo: maybe map rotation?
//                            case GOLDMINE -> {
//                                gameTile.setFill(Color.BLUEVIOLET);
//                                gameTile.setOnMouseClicked(null);
//                            }
//                        }
//                    });
//                })
//                .withMapping(PDUType.GAMESTART.getID(), p -> appendChatMessage("System", "Game has started"))
//                .withMapping(PDUType.GAMEEND.getID(), p -> {
//                    appendChatMessage("System", "Game has ended");
//                    appendChatMessage("System", "Exiting ...");
//                    listen = false;
//                    try {
//                        Thread.sleep(4000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    System.exit(0);
//                })
//                .withMapping(PDUType.SERVERTICKUPDATE.getID(), p -> Platform.runLater(() -> {
//                    hud_gold_count.setText(String.format("Gold: %s", p.decode().get(0)));
//                    hud_gold_per_second.setText(String.format("Gold/s: %s", p.decode().get(1)));
//                }));

//        new Thread(() -> {
//            while(listen){
//                byte [] data = new byte[1024];
//                DatagramPacket packet = new DatagramPacket(data, data.length);
//                try {
                    //Attempt to reconnect until a ClientID is obtained
//                    if(clientID == null)
//                        sendUnicast(new PDUBody(PDUType.JOIN.getID()));
//                    socket.receive(packet);
//                    actionHandler.map(packet);
//                }
//                catch (SocketTimeoutException e) {
//                    e.printStackTrace();
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                    break;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            socket.close();
//        }).start();

        btn_send_chat_msg.setOnAction(e -> {
            if(tf_msg.getText().isBlank() || clientID == null)
                return;
//            sendUnicast(new PDUBody(PDUType.CHATMESSAGE.getID(), clientID.toString(), tf_msg.getText()));
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text text = new Text(String.format("[You]: %s", tf_msg.getText()));
            TextFlow textFlow = new TextFlow(text);

//            textFlow.setStyle("-fx-color: rgb(239, 242, 255); -fx-background-color: rgb(15, 125, 242); -fx-background-radius: 20px");

            textFlow.setPadding(new Insets(5, 10, 5, 10));
//            text.setFill(Color.color(0.934, 0.945, 0.966));

            hBox.getChildren().addAll(textFlow);
            vb_msg.getChildren().add(hBox);
            tf_msg.clear();
        });

//        lbl_info.setOnAction(e -> sendUnicast(new MyPDU00Join()));
    }

    public static void sendUnicast(PDU p){
//        DatagramPacket packet = new DatagramPacket(p.getByteBuffer(), p.getByteBuffer().length, serverIP, serverPort);
//        try {
//            socket.send(packet);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void appendChatMessage(String senderName, String msg){
        Platform.runLater(() -> {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text text = new Text(String.format("[%s]: %s", senderName, msg));
            TextFlow textFlow = new TextFlow(text);
//            textFlow.setStyle("-fx-background-color: rgb(233, 233, 235); -fx-background-radius: 20px");
            textFlow.setPadding(new Insets(5, 10, 5, 10));
            hBox.getChildren().addAll(textFlow);
            vb_msg.getChildren().add(hBox);
        });
    }

    private void initMap(){
        for (byte i = 0; i < 11; i++)
            for (byte j = 0; j < 11; j++) {
                //Water
                if(j == 5) {
//                    Rectangle waterTile = new Rectangle(40, 40, Color.valueOf("#006994"));
//                    final byte iFin = i, jFin = j;
//                    waterTile.setOnMouseClicked((MouseEvent t) -> sendUnicast(PDUBody.fromPlayerMoveData(clientID, iFin, jFin, (byte) 1)));
//                    game_map.add(waterTile, i, j);

                    continue;
                }
                //Basic tile
                game_map.add(new Rectangle(40, 40, Color.TRANSPARENT), i, j);
            }
    }

//    public static void addLabel(String messageFromServer, VBox vBox){
//        HBox hBox = new HBox();
//        hBox.setAlignment(Pos.CENTER_LEFT);
//        hBox.setPadding(new Insets(5, 5, 5, 10));
//
//        Text text = new Text(messageFromServer);
//        TextFlow textFlow = new TextFlow(text);
//        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235); -fx-background-radius: 20px");
//        textFlow.setPadding(new Insets(5, 10, 5, 10));
//        hBox.getChildren().addAll(textFlow);
//    }
}
