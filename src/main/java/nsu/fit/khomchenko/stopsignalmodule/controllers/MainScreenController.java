package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;
import nsu.fit.khomchenko.stopsignalmodule.utils.HuntStatisticsCalculator;
import nsu.fit.khomchenko.stopsignalmodule.utils.OddBallStatisticsCalculator;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MainScreenController {
    @FXML
    public Label schemaChoiceLabel;

    @FXML
    public ComboBox<DatabaseSchema> schemaChoiceComboBox;
    @FXML
    private MainController mainController;

    @FXML
    public Button startButton;

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
    public void handleSchemaChoiceComboBoxAction() {
        DatabaseSchema selectedSchema = schemaChoiceComboBox.getValue();

        if (selectedSchema != null) {
            System.out.println("Выбрана новая схема: " + selectedSchema.getDisplayName());
        }
    }

    private String handleDialogTestInterface(File selectedFile, DatabaseSchema selectedSchema) {
        TextInputDialog tableNameDialog = new TextInputDialog();
        tableNameDialog.setTitle("Испытуемый");
        tableNameDialog.setHeaderText("Введите имя испытуемого:");

        Optional<String> testPerson = tableNameDialog.showAndWait();
        String baseTableName = testPerson.orElse("").trim();

        String[] choices = {"М", "Ж"};
        ChoiceDialog<String> genderDialog = new ChoiceDialog<>("М", Arrays.asList(choices));
        genderDialog.setTitle("Пол");
        genderDialog.setHeaderText("Выберите пол (М/Ж):");
        genderDialog.setContentText("Пол:");

        Optional<String> genderResult = genderDialog.showAndWait();
        String gender = genderResult.orElse("");

        TextInputDialog ageDialog = new TextInputDialog();
        ageDialog.setTitle("Возраст");
        ageDialog.setHeaderText("Введите возраст (0-120):");
        ageDialog.setContentText("Возраст:");

        Optional<String> ageResult = ageDialog.showAndWait();
        int age = ageResult.map(Integer::parseInt).orElse(-1);

        if (baseTableName.isEmpty() || !Arrays.asList(choices).contains(gender) || age < 0 || age > 120) {
            return baseTableName;
        }

        String tableName = baseTableName + "_" + gender + "_" + age + "_test";

        String schemaName = selectedSchema.getSchemaName();

        String filePath = selectedFile.getAbsolutePath();

        DatabaseHandler.loadAndSaveData(filePath, tableName, schemaName);

        return tableName;
    }

    @FXML
    private void handleStartButtonAction(ActionEvent actionEvent) {
        DatabaseSchema selectedSchema = schemaChoiceComboBox.getValue();

        if (selectedSchema != null) {
            System.out.println("Выбрана схема: " + selectedSchema.getDisplayName());

            handleSchemaChoiceComboBoxAction();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл IQDAT");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файлы IQDAT", "*.iqdat"));

            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {

                String tableName = handleDialogTestInterface(selectedFile, selectedSchema);
                switch (selectedSchema) {
                    case HUNT -> {
                        List<HuntData> huntDataList = DatabaseHandler.getHuntDataForTable(selectedSchema, tableName);
                        if (!huntDataList.isEmpty()) {
                            List<String> statisticHuntsResult = HuntStatisticsCalculator.calculateStatistics(huntDataList, tableName, selectedSchema, false);
                            mainController.switchToStatistic();
                            mainController.getStatisticsController().displayStatistics(statisticHuntsResult);
                        } else {
                            System.out.println("Нет данных для таблицы " + tableName + " в схеме " + selectedSchema);
                        }
                    }
                    case ODD_BALL_EASY, ODD_BALL_HARD -> {
                        List<OddBallData> oddBallDataList = DatabaseHandler.getOddBallDataForSchema(selectedSchema, tableName);
                        if (!oddBallDataList.isEmpty()) {
                            List<String> statisticsOddBallResult = OddBallStatisticsCalculator.calculateStatistics(oddBallDataList, tableName, selectedSchema, false);
                            mainController.switchToStatistic();
                            mainController.getStatisticsController().displayStatistics(statisticsOddBallResult);
                        } else {
                            System.out.println("Нет данных для таблицы " + tableName + " в схеме " + selectedSchema.getSchemaName());
                        }
                    }
                    default -> System.out.println("Неизвестная схема: " + selectedSchema.getSchemaName());
                }
                mainController.closeMenuItem.setVisible(true);


            } else {
                showAlert("Файл не выбран.");
            }

        } else {
            showAlert("Выберите схему перед началом.");
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

        List<DatabaseSchema> schemaList = Arrays.asList(DatabaseSchema.values());
        schemaChoiceComboBox.getItems().addAll(schemaList);

        schemaChoiceComboBox.setValue(DatabaseSchema.HUNT);
    }
}