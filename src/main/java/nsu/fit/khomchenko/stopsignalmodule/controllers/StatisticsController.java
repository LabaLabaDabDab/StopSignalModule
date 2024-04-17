package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;

public class StatisticsController {
    @FXML
    public TableView statisticTable;
    public TextField successfulStopsLowerTextField;
    public TextField successfulStopsUpperTextField;
    public TextField missedHitsLowerTextField;
    public TextField missedHitsUpperTextField;
    public TextField incorrectHitsLowerTextField;
    public TextField incorrectHitsUpperTextField;
    public TextField correctHitsLowerTextField;
    public TextField correctHitsUpperTextField;
    public TextField averageTimeLowerTextField;
    public TextField averageTimeUpperTextField;
    public TextField timeDispersionLowerTextField;
    public TextField timeDispersionUpperTextField;
    @FXML
    private Label statLabel;

    @FXML
    private DatabaseSchema selectedSchema;

    @FXML
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void displayStatistics(String statisticsResult) {
        statLabel.setText(statisticsResult);
    }

    @FXML
    private void initialize() {

    };
}
