package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;

public class MainScreenController {
    @FXML
    public Label schemaChoiceLabel;

    @FXML
    public Button startButton;

    @FXML
    public ComboBox<DatabaseSchema> schemaChoiceComboBox;

    @FXML
    private void handleStartButtonAction(ActionEvent actionEvent) {
        DatabaseSchema selectedSchema = schemaChoiceComboBox.getValue();

        if (selectedSchema != null) {
            System.out.println("Выбрана схема: " + selectedSchema.getDisplayName());

        } else {
            showAlert("Выберите схему перед началом.");
        }
    }

    @FXML
    private void handleSchemaChoiceComboBoxAction() {
        DatabaseSchema selectedSchema = schemaChoiceComboBox.getValue();

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

}
