package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;

import java.util.List;

public class StatisticsController {
    @FXML
    public TableView statisticTable;

    @FXML
    public TextField successfulStopsLowerTextField;
    @FXML
    public TextField successfulStopsUpperTextField;
    @FXML
    public TextField missedHitsLowerTextField;
    @FXML
    public TextField missedHitsUpperTextField;
    @FXML
    public TextField incorrectHitsLowerTextField;
    @FXML
    public TextField incorrectHitsUpperTextField;
    @FXML
    public TextField correctHitsLowerTextField;
    @FXML
    public TextField correctHitsUpperTextField;
    @FXML
    public TextField averageTimeLowerTextField;
    @FXML
    public TextField averageTimeUpperTextField;
    @FXML
    public TextField timeDispersionLowerTextField;
    @FXML
    public TextField timeDispersionUpperTextField;
    @FXML
    public Label subject;


    @FXML
    private Label successfulStopsLabel;
    @FXML
    private Label missedHitsLabel;
    @FXML
    private Label incorrectHitsLabel;
    @FXML
    private Label correctHitsLabel;
    @FXML
    private Label averageTimeLabel;
    @FXML
    private Label timeDispersionLabel;

    @FXML
    private Label statLabel;

    @FXML
    private DatabaseSchema selectedSchema;

    @FXML
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void displayStatistics(List<String> statisticsResult) {
        successfulStopsLabel.setText(statisticsResult.get(0));
        successfulStopsLabel.setTextFill(Color.BLACK);

        successfulStopsLabel.setText(statisticsResult.get(1));
        successfulStopsLabel.setTextFill(Color.GREEN);

        missedHitsLabel.setText(statisticsResult.get(2));
        missedHitsLabel.setTextFill(Color.GREEN);

        incorrectHitsLabel.setText(statisticsResult.get(3));
        incorrectHitsLabel.setTextFill(Color.GREEN);

        correctHitsLabel.setText(statisticsResult.get(4));
        correctHitsLabel.setTextFill(Color.GREEN);

        averageTimeLabel.setText(statisticsResult.get(5));
        averageTimeLabel.setTextFill(Color.GREEN);

        timeDispersionLabel.setText(statisticsResult.get(6));
        timeDispersionLabel.setTextFill(Color.GREEN);
    }

    @FXML
    private void initialize() {

    };
}