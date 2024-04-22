package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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

import static nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler.*;

public class StatisticsController {
    @FXML
    public TextField ageLowerTextField;
    @FXML
    public TextField ageUpperTextField;

    @FXML
    public VBox VboxForData;
    @FXML
    public Button applyButton;


    @FXML
    private CheckBox maleCheckBox;

    @FXML
    private CheckBox femaleCheckBox;

    private DatabaseSchema selectedSchema;

    private DatabaseHandler databaseHandler;

    private Map<String, Map<String, String>> statisticsResult;

    @FXML
    private MainController mainController;


    public void setSelectedSchema(DatabaseSchema schema) {
        this.selectedSchema = schema;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStatisticsResult(Map<String, Map<String, String>> statisticsResult) {
        this.statisticsResult = statisticsResult;
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

    public void displayStatisticsLoading() {
        VboxForData.getChildren().clear();

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setProgress(-1);
        VboxForData.getChildren().add(loadingIndicator);
    }

    public void displayStatistics(Map<String, Double> averageStatisticsResultFinal , Map<String, Double> standardDeviation) {
        VboxForData.getChildren().clear();

        if (statisticsResult == null || averageStatisticsResultFinal == null || standardDeviation == null) {
            displayStatisticsLoading();
            return;
        }

        Map<String, String> participantInfo = statisticsResult.get("participant_info");
        if (participantInfo == null) {
            System.err.println("Отсутствует информация об испытуемом.");
            return;
        }

        String gender = participantInfo.get("gender");
        String ageString = participantInfo.get("age");
        String testName = participantInfo.get("testName");

        if (gender == null || ageString == null || testName == null) {
            System.err.println("Отсутствует пол, возраст или имя испытуемого.");
            return;
        }

        int age = 0;
        try {
            age = Integer.parseInt(ageString);
        } catch (NumberFormatException e) {
            System.err.println("Неверный формат возраста.");
            e.printStackTrace();
        }
        
        Label participantInfoLabel = new Label("Информация об испытуемом:");
        participantInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        participantInfoLabel.setPadding(new Insets(5));
        VboxForData.getChildren().add(participantInfoLabel);

        Label infoLabel = new Label("Пол: " + gender + ", Имя: " + testName + ", Возраст: " + age);
        infoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        infoLabel.setPadding(new Insets(5));
        VboxForData.getChildren().add(infoLabel);

        Font fontData = Font.font("Arial", FontWeight.BOLD, 25);
        Insets labelData = new Insets(5);

        Font fontStatisticData = Font.font("Arial", FontWeight.BOLD, 16);
        Insets labelStatisticData = new Insets(5);


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
        VboxForData.getChildren().add(new Label(" "));

        Label labelInfo = new Label("Статистика для группы: ");
        labelInfo.setFont(fontData);
        labelInfo.setTextAlignment(TextAlignment.LEFT);
        labelInfo.setWrapText(true);
        labelInfo.setPadding(labelData);

        VboxForData.getChildren().add(labelInfo);


        for (Map.Entry<String, Double> entry : averageStatisticsResultFinal.entrySet()) {
            String columnName = entry.getKey();
            Double averageValue = entry.getValue();

            Map<String, String> columnData = statisticsResult.get(columnName);
            String comment = (columnData != null) ? columnData.getOrDefault("comment", "") : "";

            Label averageLabel = new Label(comment + " (Среднее): " + averageValue);
            averageLabel.setFont(fontStatisticData);
            averageLabel.setTextAlignment(TextAlignment.LEFT);
            averageLabel.setWrapText(true);
            averageLabel.setPadding(labelStatisticData);

            VboxForData.getChildren().add(averageLabel);
        }

        VboxForData.getChildren().add(new Label(" "));

        for (Map.Entry<String, Double> entry : standardDeviation.entrySet()) {
            String columnName = entry.getKey();
            Double stdDeviation = entry.getValue();

            Map<String, String> columnData = statisticsResult.get(columnName);
            String comment = (columnData != null) ? columnData.getOrDefault("comment", "") : "";

            Label stdDevLabel = new Label(comment + " (Стандартное отклонение): " + stdDeviation);
            stdDevLabel.setFont(fontStatisticData);
            stdDevLabel.setTextAlignment(TextAlignment.LEFT);
            stdDevLabel.setWrapText(true);
            stdDevLabel.setPadding(labelStatisticData);

            VboxForData.getChildren().add(stdDevLabel);

            Region separator = new Region();
            separator.setPrefHeight(180);
            VboxForData.getChildren().add(separator);
        }

    }

    @FXML
    private void applyFilters(ActionEvent event) {
        int lowerAge = Integer.parseInt(ageLowerTextField.getText());
        int upperAge = Integer.parseInt(ageUpperTextField.getText());

        boolean isMaleSelected = maleCheckBox.isSelected();
        boolean isFemaleSelected = femaleCheckBox.isSelected();

        CompletableFuture<Integer> countByGroupFuture = CompletableFuture.supplyAsync(() -> countByGroup(selectedSchema, isMaleSelected, isFemaleSelected, lowerAge, upperAge));
        CompletableFuture<Map<String, Double>> averageStatisticsFuture = CompletableFuture.supplyAsync(() -> getAverageStatistics(selectedSchema, isMaleSelected, isFemaleSelected, lowerAge, upperAge));

        CompletableFuture<Map<String, Double>> standardDeviationFuture = CompletableFuture.allOf(countByGroupFuture, averageStatisticsFuture)
                .thenApplyAsync(ignored -> getStandardDeviationStatistics(selectedSchema, isMaleSelected, isFemaleSelected, lowerAge, upperAge, averageStatisticsFuture.join(), countByGroupFuture.join()));

        standardDeviationFuture.thenAcceptAsync(standardDeviation -> {
            Platform.runLater(() -> displayStatistics(averageStatisticsFuture.join(), standardDeviation));
        });
    }

    @FXML
    private void initialize() {
        maleCheckBox.setSelected(true);
        femaleCheckBox.setSelected(true);

        ageLowerTextField.setText("0");
        ageUpperTextField.setText("118");
    }
}