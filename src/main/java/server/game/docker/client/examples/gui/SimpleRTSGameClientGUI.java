package server.game.docker.client.examples.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.game.docker.client.GameClient;

import java.nio.file.Path;

public class SimpleRTSGameClientGUI extends Application {
    static GameClient gameClient;
    private Scene menuScene, gameSessionScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            gameClient = new GameClient(new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
            //Game crash
            stop();
        }
        primaryStage.setTitle("Simple RTS Game GUI Client");
        menuScene = new Scene(new FXMLLoader(Path.of("src/main/java/server/game/docker/client/examples/gui/fxml/menu.fxml").toUri().toURL()).load());
        gameSessionScene = new Scene(new FXMLLoader(Path.of("src/main/java/server/game/docker/client/examples/gui/fxml/session.fxml").toUri().toURL()).load());
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        gameClient.disconnect();
//        if(clientID != null)
//            sendUnicast(new PDUBody(PDUType.DISCONNECT.getID(), clientID.toString()));
//        listen = false;
        System.out.println("Exited client ...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
