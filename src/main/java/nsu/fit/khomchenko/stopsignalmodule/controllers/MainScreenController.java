package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainScreenController {
    @FXML
    public Label schemaChoiceLabel;

    @FXML
    public Button startButton;

    @FXML
    public ComboBox<DatabaseSchema> schemaChoiceComboBox;
    @FXML
    public MainController mainController;

    @FXML
    private MainScreenController mainScreenController;

    @FXML
    private TableController tableController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setTableController(TableController tableController) {
        this.tableController = tableController;
    }

    @FXML
    private void handleStartButtonAction(ActionEvent actionEvent) {
        DatabaseSchema selectedSchema = schemaChoiceComboBox.getValue();

        if (selectedSchema != null) {
            System.out.println("Выбрана схема: " + selectedSchema.getDisplayName());

        } else {
            showAlert("Выберите схему перед началом.");
        }
    }

    private void openStatisticsScene(DatabaseSchema selectedSchema) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nsu/fit/khomchenko/stopsignalmodule/statistics.fxml"));
            Parent root = loader.load();


            StatisticsController statisticsController = loader.getController();
            //statisticsController.initializeData(selectedSchema);

            Scene statisticsScene = new Scene(root);
            Stage statisticsStage = new Stage();

            statisticsStage.setScene(statisticsScene);
            statisticsStage.setTitle("Статистика");
            statisticsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSchemaChoiceComboBoxAction() {
        DatabaseSchema selectedSchema = mainScreenController.schemaChoiceComboBox.getValue();

        if (selectedSchema != null) {
            System.out.println("Выбрана новая схема: " + selectedSchema.getDisplayName());
        }
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    @FXML
    private void initialize() {
        mainScreenController = this;

        List<DatabaseSchema> schemaList = Arrays.asList(DatabaseSchema.values());
        schemaChoiceComboBox.getItems().addAll(schemaList);

        schemaChoiceComboBox.setValue(DatabaseSchema.HUNT);
    }
}
