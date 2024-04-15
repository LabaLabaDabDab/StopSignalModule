package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.utils.HuntStatisticsCalculator;
import nsu.fit.khomchenko.stopsignalmodule.utils.OddBallStatisticsCalculator;

public class StatisticsController {

    @FXML
    private Label statLabel;
    private DatabaseSchema selectedSchema;

    @FXML
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    /*public void initializeData(DatabaseSchema selectedSchema) {
        this.selectedSchema = selectedSchema;

        if (selectedSchema == DatabaseSchema.HUNT) {
            statLabel.setText(HuntStatisticsCalculator.calculateStatistics());
        } else if (selectedSchema == DatabaseSchema.ODD_BALL_EASY || selectedSchema == DatabaseSchema.ODD_BALL_HARD) {
            statLabel.setText(OddBallStatisticsCalculator.calculateStatistics());
        }
    }*/
}
