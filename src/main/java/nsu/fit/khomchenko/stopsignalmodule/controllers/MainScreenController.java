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
import nsu.fit.khomchenko.stopsignalmodule.data.StroopData;
import nsu.fit.khomchenko.stopsignalmodule.utils.HuntStatisticsCalculator;
import nsu.fit.khomchenko.stopsignalmodule.utils.InputDialogHelper;
import nsu.fit.khomchenko.stopsignalmodule.utils.OddBallStatisticsCalculator;
import nsu.fit.khomchenko.stopsignalmodule.utils.StroopStatisticsCalculator;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler.*;

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

    @FXML
    private StatisticsController statisticsController;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setTableController(TableController tableController) {
        this.tableController = tableController;
    }

    public void setStatisticsController(StatisticsController statisticsController) {
        this.statisticsController = statisticsController;
    }


    @FXML
    public void handleSchemaChoiceComboBoxAction() {
        DatabaseSchema selectedSchema = schemaChoiceComboBox.getValue();

        if (selectedSchema != null) {
            System.out.println("Выбрана новая схема: " + selectedSchema.getDisplayName());
        }
    }

    private CompletableFuture<Optional<String>> handleDialogTestInterfaceAsync(File selectedFile, DatabaseSchema selectedSchema) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<String> testName = InputDialogHelper.promptTestPersonName();
            if (testName.isEmpty()) {
                return Optional.empty();
            }

            Optional<String> gender = InputDialogHelper.promptGender();
            if (gender.isEmpty()) {
                return Optional.empty();
            }

            Optional<Integer> age = InputDialogHelper.promptAge();
            if (age.isEmpty()) {
                return Optional.empty();
            }

            String tableName = testName.get() + "_" + gender.get() + "_" + age.get() + "_test";
            String schemaName = selectedSchema.getSchemaName();
            String filePath = selectedFile.getAbsolutePath();

            DatabaseHandler.loadAndSaveData(filePath, tableName, schemaName);

            return Optional.of(tableName);
        }, Platform::runLater);
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
                CompletableFuture<Optional<String>> tableNameFuture = handleDialogTestInterfaceAsync(selectedFile, selectedSchema);

                tableNameFuture.thenAcceptAsync(tableNameOptional -> {
                    if (tableNameOptional.isPresent()) {
                        String tableName = tableNameOptional.get();
                        CompletableFuture<Map<String, Double>> averageStatisticsFuture = null;
                        CompletableFuture<Map<String, Map<String, String>>> statisticsResultFuture = null;
                        CompletableFuture<Integer> countByGroupFuture = null;

                        switch (selectedSchema) {
                            case HUNT -> {
                                List<HuntData> huntDataList = DatabaseHandler.getHuntDataForTable(selectedSchema, tableName);
                                if (!huntDataList.isEmpty()) {
                                    countByGroupFuture = CompletableFuture.supplyAsync(() -> countByGroup(selectedSchema, true, true, 0, 120));
                                    averageStatisticsFuture = CompletableFuture.supplyAsync(() -> getAverageStatistics(selectedSchema, true, true, 0, 120));
                                    statisticsResultFuture = CompletableFuture.supplyAsync(() -> HuntStatisticsCalculator.calculateStatistics(huntDataList, tableName, selectedSchema, false));
                                } else {
                                    System.out.println("Нет данных для таблицы " + tableName + " в схеме " + selectedSchema);
                                }
                            }
                            case ODD_BALL_EASY, ODD_BALL_HARD -> {
                                List<OddBallData> oddBallDataList = DatabaseHandler.getOddBallDataForSchema(selectedSchema, tableName);
                                if (!oddBallDataList.isEmpty()) {
                                    countByGroupFuture = CompletableFuture.supplyAsync(() -> countByGroup(selectedSchema, true, true, 0, 120));
                                    averageStatisticsFuture = CompletableFuture.supplyAsync(() -> getAverageStatistics(selectedSchema, true, true, 0, 120));
                                    statisticsResultFuture = CompletableFuture.supplyAsync(() -> OddBallStatisticsCalculator.calculateStatistics(oddBallDataList, tableName, selectedSchema, false));
                                } else {
                                    System.out.println("Нет данных для таблицы " + tableName + " в схеме " + selectedSchema.getSchemaName());
                                }
                            }
                            case STROOP -> {
                                List<StroopData> stroopDataList = DatabaseHandler.getStroopDataForSchema(selectedSchema, tableName);
                                if (!stroopDataList.isEmpty()) {
                                    countByGroupFuture = CompletableFuture.supplyAsync(() -> countByGroup(selectedSchema, true, true, 0, 120));
                                    averageStatisticsFuture = CompletableFuture.supplyAsync(() -> getAverageStatistics(selectedSchema, true, true, 0, 120));
                                    statisticsResultFuture = CompletableFuture.supplyAsync(() -> StroopStatisticsCalculator.calculateStatistics(stroopDataList, tableName, selectedSchema, false));
                                } else {
                                    System.out.println("Нет данных для таблицы " + tableName + " в схеме " + selectedSchema.getSchemaName());
                                }
                            }
                            default -> System.out.println("Неизвестная схема: " + selectedSchema.getSchemaName());
                        }

                        if (averageStatisticsFuture != null && statisticsResultFuture != null) {
                            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(averageStatisticsFuture, statisticsResultFuture, countByGroupFuture);

                            final CompletableFuture<Map<String, Double>> finalAverageStatisticsFuture = averageStatisticsFuture;
                            final CompletableFuture<Map<String, Map<String, String>>> finalStatisticsResultFuture = statisticsResultFuture;
                            final CompletableFuture<Integer> finalCountByGroupFuture = countByGroupFuture;

                            combinedFuture.thenRunAsync(() -> {
                                try {
                                    Map<String, Double> averageStatisticsResultFinal = finalAverageStatisticsFuture.get();
                                    Map<String, Map<String, String>> statisticsResultFinal = finalStatisticsResultFuture.get();
                                    Integer countByGroupFutureFinal = finalCountByGroupFuture.get();

                                    Map<String, Double> standardDeviation = getStandardDeviationStatistics(selectedSchema, true, true, 0, 120, averageStatisticsResultFinal, countByGroupFutureFinal);

                                    mainController.switchToStatistic();
                                    mainController.getStatisticsController().setSelectedTableName(tableName);
                                    mainController.getStatisticsController().setStatisticsResult(statisticsResultFinal);
                                    mainController.getStatisticsController().setSelectedSchema(selectedSchema);
                                    mainController.getStatisticsController().displayStatistics(averageStatisticsResultFinal, standardDeviation);

                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }, Platform::runLater);
                        }
                    } else {
                        showAlert("Не удалось получить название таблицы.");
                    }
                }, Platform::runLater);
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