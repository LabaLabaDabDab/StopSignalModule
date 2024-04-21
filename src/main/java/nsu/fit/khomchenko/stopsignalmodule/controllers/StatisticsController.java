package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import org.apache.commons.math3.distribution.TDistribution;

import java.util.*;
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

    public Map<String, Double> calculateConfidenceInterval(Map<String, Double> averageStatistics, Map<String, Double> standardDeviation, double confidenceLevel) {
        Map<String, Double> confidenceInterval = new HashMap<>();

        int degreesOfFreedom = averageStatistics.size() - 1;

        TDistribution tDistribution = new TDistribution(degreesOfFreedom);

        for (Map.Entry<String, Double> entry : averageStatistics.entrySet()) {
            String columnName = entry.getKey();
            Double average = entry.getValue();
            Double stdDev = standardDeviation.get(columnName);

            if (average != null && stdDev != null) {
                double tValue = tDistribution.inverseCumulativeProbability(1 - (1 - confidenceLevel) / 2);
                double marginOfError = stdDev * tValue / Math.sqrt(average);
                confidenceInterval.put(columnName, marginOfError);
            }
        }

        return confidenceInterval;
    }


    public void displayStatistics(Map<String, Map<String, String>> statisticsResult, Map<String, Double> averageStatisticsResultFinal , Map<String, Double> standardDeviation) {
        VboxForData.getChildren().clear();
        VboxDelta.getChildren().clear();

        Font fontData = Font.font("Arial", FontWeight.BOLD, 20);
        Insets labelData = new Insets(5);


        Set<String> columnNames = averageStatisticsResultFinal.keySet();

        List<String> columnDataNames = new ArrayList<>();
        for (String columnName : columnNames) {
            if (statisticsResult.containsKey(columnName)) {
                columnDataNames.add(columnName);
            }
        }

        Map<String, Double> confidenceInterval = calculateConfidenceInterval(averageStatisticsResultFinal, standardDeviation, 0.95);

        for (String columnName : columnDataNames) {
            Map<String, String> columnData = statisticsResult.get(columnName);
            String comment = columnData.get("comment");
            String valueString = columnData.get("value");
            valueString = valueString.replaceAll("%", "");
            Double value = Double.parseDouble(valueString);

            Label label = new Label(comment + ": " + valueString);
            label.setFont(fontData);
            label.setTextAlignment(TextAlignment.LEFT);
            label.setWrapText(true);
            label.setPadding(labelData);

            if (confidenceInterval.containsKey(columnName)) {
                Double marginOfError = confidenceInterval.get(columnName);
                Double averageValue = averageStatisticsResultFinal.get(columnName);

                if (averageValue != null) {
                    if (value >= averageValue - marginOfError && value <= averageValue + marginOfError) {
                        label.setTextFill(Color.GREEN);
                    } else {
                        label.setTextFill(Color.RED);
                    }
                } else {
                    System.err.println("Среднее значение для " + columnName + " равно null.");
                }
            }

            VboxForData.getChildren().add(label);
        }

        VboxForData.getChildren().add(new Label(" "));

        for (Map.Entry<String, Double> entry : averageStatisticsResultFinal.entrySet()) {
            String columnName = entry.getKey();
            Double averageValue = entry.getValue();

            Map<String, String> columnData = statisticsResult.get(columnName);
            String comment = "";
            if (columnData != null) {
                comment = columnData.get("comment");
            }

            Label averageLabel = new Label(comment + " (Среднее): " + averageValue);
            averageLabel.setFont(fontData);
            averageLabel.setTextAlignment(TextAlignment.LEFT);
            averageLabel.setWrapText(true);
            averageLabel.setPadding(labelData);

            Double stdDeviation = standardDeviation.get(columnName);

            Label stdDevLabel = new Label(comment + " (Стандартное отклонение): " + stdDeviation);
            stdDevLabel.setFont(fontData);
            stdDevLabel.setTextAlignment(TextAlignment.LEFT);
            stdDevLabel.setWrapText(true);
            stdDevLabel.setPadding(labelData);

            VboxForData.getChildren().addAll(averageLabel, stdDevLabel);

            Region separator = new Region();
            separator.setPrefHeight(200);
            VboxForData.getChildren().add(separator);
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