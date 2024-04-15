package nsu.fit.khomchenko.stopsignalmodule;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nsu.fit.khomchenko.stopsignalmodule.controllers.MainController;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        MainController controller = fxmlLoader.getController();
        controller.setScene(scene);

        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);

        primaryStage.setTitle("TestModule");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
