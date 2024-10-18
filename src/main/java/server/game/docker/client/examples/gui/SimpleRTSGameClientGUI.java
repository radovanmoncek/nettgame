package server.game.docker.client.examples.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.game.docker.client.GameSessionClient;
import server.game.docker.client.GameSessionClientAPI;

import java.nio.file.Path;

public class SimpleRTSGameClientGUI extends Application {
    private Scene menuScene, gameSessionScene;
    private GameSessionClientAPI gameClient;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Simple RTS Game GUI Client");
        FXMLLoader menuLoader = new FXMLLoader(Path.of("src/main/java/server/game/docker/client/examples/gui/fxml/menu.fxml").toUri().toURL());
        FXMLLoader sessionLoader = new FXMLLoader(Path.of("src/main/java/server/game/docker/client/examples/gui/fxml/session.fxml").toUri().toURL());
        menuLoader.setControllerFactory(type -> {
            try {
                gameClient = new GameSessionClientAPI();
                return new MenuController(gameClient);
//                Stream.of(type.getFields()).filter(f -> f.getType().equals(type)).forEach(f -> {
//                    try {
//                        f.set(f, gameClient);
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                });
//                return (Builder<?>) type.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                //Game crash
                try {
                    stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                throw new RuntimeException(e);
            }
        });
//        sessionLoader.setBuilderFactory();
        menuScene = new Scene(menuLoader.load());
        gameSessionScene = new Scene(sessionLoader.load());
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        gameClient.disconnect();
        System.out.println("Exited client ...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
