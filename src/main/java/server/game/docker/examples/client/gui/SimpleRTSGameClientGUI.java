package server.game.docker.examples.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDUTypes;

import java.nio.file.Path;

import static server.game.docker.examples.client.gui.IndexController.*;

public class SimpleRTSGameClientGUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Simple RTS Game GUI Client");
        primaryStage.setScene(new Scene(new FXMLLoader(Path.of("src/main/java/server/game/docker/examples/client/gui/fxml/index.fxml").toUri().toURL()).load()));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if(clientID != null)
            sendUnicast(new MyPDU(MyPDUTypes.DISCONNECT.getID(), clientID.toString()));
        listen = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
