package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private MenuController menuController;

    @FXML
    private MainScreenController mainScreenController;

    @FXML
    private TableController tableController;

    @FXML
    private BorderPane borderPane;

    public Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public MainController getMainController() {
        return this;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public void loadFXML(String fxmlPath, BorderPane pane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));  // Добавьте "/" перед путем
            Parent root = loader.load();
            pane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void loadMenu() {
        loadFXML("nsu/fit/khomchenko/stopsignalmodule/menu.fxml", borderPane);
        menuController.setMainController(this);
    }

    /*
    @FXML
    private void loadMainScreen() {
        loadFXML("mainScreen.fxml", borderPane);
        mainScreenController.setMainController(this);
    }
    /
     */

    @FXML
    private void initialize() {
        loadMenu();
        //loadMainScreen();
    }

}