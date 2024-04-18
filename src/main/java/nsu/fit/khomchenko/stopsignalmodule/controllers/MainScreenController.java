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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler.getAverageStatistics;

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
                return Optional.<String>empty();
            }

            String tableName = baseTableName + "_" + gender + "_" + age + "_test";

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

                        switch (selectedSchema) {
                            case HUNT -> {
                                List<HuntData> huntDataList = DatabaseHandler.getHuntDataForTable(selectedSchema, tableName);
                                if (!huntDataList.isEmpty()) {
                                    averageStatisticsFuture = CompletableFuture.supplyAsync(() -> getAverageStatistics(selectedSchema, true, true, 0, 120));
                                    statisticsResultFuture = CompletableFuture.supplyAsync(() -> HuntStatisticsCalculator.calculateStatistics(huntDataList, tableName, selectedSchema, false));
                                } else {
                                    System.out.println("Нет данных для таблицы " + tableName + " в схеме " + selectedSchema);
                                }
                            }
                            case ODD_BALL_EASY, ODD_BALL_HARD -> {
                                List<OddBallData> oddBallDataList = DatabaseHandler.getOddBallDataForSchema(selectedSchema, tableName);
                                if (!oddBallDataList.isEmpty()) {
                                    averageStatisticsFuture = CompletableFuture.supplyAsync(() -> getAverageStatistics(selectedSchema, true, true, 0, 120));
                                    statisticsResultFuture = CompletableFuture.supplyAsync(() -> OddBallStatisticsCalculator.calculateStatistics(oddBallDataList, tableName, selectedSchema, false));
                                } else {
                                    System.out.println("Нет данных для таблицы " + tableName + " в схеме " + selectedSchema.getSchemaName());
                                }
                            }
                            default -> System.out.println("Неизвестная схема: " + selectedSchema.getSchemaName());
                        }

                        if (averageStatisticsFuture != null && statisticsResultFuture != null) {
                            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(averageStatisticsFuture, statisticsResultFuture);

                            final CompletableFuture<Map<String, Double>> finalAverageStatisticsFuture = averageStatisticsFuture;
                            final CompletableFuture<Map<String, Map<String, String>>> finalStatisticsResultFuture = statisticsResultFuture;

                            combinedFuture.thenRunAsync(() -> {
                                try {
                                    Map<String, Double> averageStatisticsResultFinal = finalAverageStatisticsFuture.get();
                                    Map<String, Map<String, String>> statisticsResultFinal = finalStatisticsResultFuture.get();
                                    mainController.switchToStatistic();
                                    mainController.getStatisticsController().displayStatistics(statisticsResultFinal, averageStatisticsResultFinal);

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