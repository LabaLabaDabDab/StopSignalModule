package nsu.fit.khomchenko.stopsignalmodule;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nsu.fit.khomchenko.stopsignalmodule.controllers.MainController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javafx.embed.swing.SwingFXUtils;


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

        primaryStage.setTitle("StopSignalModule");

        try (InputStream iconStream = MainApplication.class.getResourceAsStream("/icons/stop_icon.png")) {
            assert iconStream != null;
            BufferedImage image = ImageIO.read(iconStream);
            primaryStage.getIcons().add(SwingFXUtils.toFXImage(image, null));
        } catch (IOException e) {
            System.out.println("Не удалось загрузить иконку: " + e.getMessage());
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}
