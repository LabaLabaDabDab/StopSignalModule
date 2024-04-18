package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StatisticsController {
    @FXML
    public TextField ageLowerTextField;
    @FXML
    public TextField ageUpperTextField;

    @FXML
    public VBox VboxForData;

    @FXML
    public VBox VboxDelta;

    @FXML
    private CheckBox maleCheckBox;

    @FXML
    private CheckBox femaleCheckBox;


    private DatabaseSchema selectedSchema;

    private DatabaseHandler databaseHandler;

    @FXML
    private MainController mainController;

    private CompletableFuture<List<Double>> averageStatisticsFuture;

    public void setSelectedSchema(DatabaseSchema schema) {
        this.selectedSchema = schema;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void displayStatistics(Map<String, Map<String, String>> statisticsResult, Map<String, Double> averageStatisticsResultFinal) {
        VboxForData.getChildren().clear();
        VboxDelta.getChildren().clear();

        Font fontDelta = Font.font("Arial", FontWeight.BOLD, 15);
        Insets labelDelta = new Insets(5);

        Font fontData = Font.font("Arial", FontWeight.BOLD, 20);
        Insets labelData = new Insets(5);

        List<String> columnNames = new ArrayList<>(averageStatisticsResultFinal.keySet());

        for (Map.Entry<String, Map<String, String>> entry : statisticsResult.entrySet()) {
            String columnName = entry.getKey();
            Map<String, String> columnData = entry.getValue();

            String comment = columnData.get("comment");
            String valueString = columnData.get("value");
            Double value = Double.parseDouble(valueString); // Преобразуем значение в числовой формат

            Label label = new Label(comment + ": " + valueString);
            label.setFont(fontData);
            label.setTextAlignment(TextAlignment.LEFT);
            label.setWrapText(true);
            label.setPadding(labelData);

            // Проверяем, попадает ли значение в пределы дельты
            boolean withinDelta = false;
            if (averageStatisticsResultFinal.containsKey(columnName)) {
                Double averageValue = averageStatisticsResultFinal.get(columnName);
                Double deltaValue = Double.parseDouble(((TextField) VboxDelta.getChildren().get(columnNames.indexOf(columnName) * 2 + 1)).getText());

                if (value >= averageValue - deltaValue && value <= averageValue + deltaValue) {
                    withinDelta = true;
                }
            }

            // Установка цвета в зависимости от того, попадает ли значение в пределы дельты
            if (withinDelta) {
                label.setTextFill(Color.GREEN); // Зеленый цвет, если значение попадает в пределы дельты
            } else {
                label.setTextFill(Color.RED); // Красный цвет, если значение не попадает в пределы дельты
            }

            VboxForData.getChildren().add(label);
        }

        for (String columnName : columnNames) {
            Label label = new Label("Δ для: " + columnName);
            label.setFont(fontDelta);
            label.setTextAlignment(TextAlignment.LEFT);
            label.setWrapText(true);
            label.setPadding(labelDelta);
            VboxDelta.getChildren().add(label);

            TextField deltaTextField = new TextField();
            deltaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*\\.?\\d*")) {
                    deltaTextField.setText(oldValue);
                }
            });

            deltaTextField.setText("30");
            VboxDelta.getChildren().add(deltaTextField);
        }
    }



    @FXML
    private void initialize() {
        maleCheckBox.setSelected(true);
        femaleCheckBox.setSelected(true);

        ageLowerTextField.setText("0");
        ageUpperTextField.setText("120");
    }
}