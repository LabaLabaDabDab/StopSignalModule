package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;
import nsu.fit.khomchenko.stopsignalmodule.utils.HuntStatisticsCalculator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {
    @FXML
    public Menu tableMenu;
    @FXML
    public MenuItem closeMenuItem;
    @FXML
    public MenuItem openTablesMenuItem;
    @FXML
    private MainController mainController;
    @FXML
    public MenuBar menuBar;
    @FXML
    private MainScreenController mainScreenController;

    @FXML
    private TableController tableController;

    @FXML
    private BorderPane borderPane;

    public Scene scene;

    @FXML
    private StatisticsController statisticsController;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public MainController getMainController() {
        return this;
    }


    public BorderPane getBorderPane() {
        return borderPane;
    }

    @FXML
    private void setLightTheme() {
        setTheme("light");
    }

    @FXML
    private void setDarkTheme() {
        setTheme("dark");
    }

    private void setTheme(String theme) {
        String stylesheet = Objects.requireNonNull(getClass().getResource("/styles/" + theme + ".css")).toExternalForm();
        mainController.scene.getStylesheets().clear();
        mainController.scene.getStylesheets().add(stylesheet);
    }


    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О приложении");
        alert.setHeaderText(null);
        alert.setContentText("Powered by Khomcha");

        alert.showAndWait();
    }

    private DatabaseSchema getSchemaByName(String displayName) {
        return Arrays.stream(DatabaseSchema.values())
                .filter(schema -> schema.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(DatabaseSchema.HUNT);
    }

    private DatabaseSchema showSchemaSelectionDialog() {
        List<String> schemaNames = Arrays.stream(DatabaseSchema.values())
                .map(DatabaseSchema::getDisplayName)
                .collect(Collectors.toList());

        ChoiceDialog<String> schemaDialog = new ChoiceDialog<>(schemaNames.get(0), schemaNames);
        schemaDialog.setTitle("Выбор методики тестирования");
        schemaDialog.setHeaderText("Выберите методику для добавления данных испытуемого:");
        schemaDialog.setContentText("Методика:");

        Optional<String> selectedSchemaName = schemaDialog.showAndWait();
        return selectedSchemaName.map(this::getSchemaByName).orElse(null);
    }

    @FXML
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Текстовые файлы (*.iqdat)", "*.iqdat");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            DatabaseSchema selectedSchema = showSchemaSelectionDialog();
            if (selectedSchema != null) {
                handleDialogInterface(selectedFile, selectedSchema);

                if (tableController != null) {
                    tableController.allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables(selectedSchema));
                    tableController.showTableList(tableController.searchField.getText());
                }
            }
        }
    }

    private void handleDialogInterface(File selectedFile, DatabaseSchema selectedSchema) {
        TextInputDialog tableNameDialog = new TextInputDialog();
        tableNameDialog.setTitle("Название таблицы");
        tableNameDialog.setHeaderText("Введите базовое название таблицы:");
        tableNameDialog.setContentText("Базовое название таблицы:");

        Optional<String> baseTableNameResult = tableNameDialog.showAndWait();
        String baseTableName = baseTableNameResult.orElse("").trim();


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
            showAlert();
            return;
        }

        String tableName = baseTableName + "_" + gender + "_" + age;
        String schemaName = selectedSchema.getSchemaName();

        String filePath = selectedFile.getAbsolutePath();
        DatabaseHandler.loadAndSaveData(filePath, tableName, schemaName);
    }

    private void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Некорректный ввод");
            alert.setContentText("Введите правильные данные.");
            alert.showAndWait();
        });
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
    private void handleDeleteTable() {
        String selectedTable = tableController.tableListView.getSelectionModel().getSelectedItem();

        if (selectedTable != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить таблицу?");
            alert.setContentText("Таблица: " + selectedTable);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = DatabaseHandler.deleteTable(tableController.schemaComboBox.getValue(), selectedTable);
                if (success) {
                    showAlert("Таблица успешно удалена.");

                    tableController.allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables(tableController.schemaComboBox.getValue()));

                    if (tableController.allTables.isEmpty()) {
                        tableController.tableListView.setVisible(false);
                        tableController.schemaLabel.setVisible(false);
                        tableController.searchField.setVisible(false);

                        tableController.tableView.setVisible(false);
                        tableController.tableNameLabel.setVisible(false);

                        Menu tableMenu = menuBar.getMenus().get(2);
                        tableMenu.setVisible(false);
                    } else {
                        tableController.showTableList(tableController.searchField.getText());

                        tableController.tableView.getItems().clear();
                        tableController.tableNameLabel.setText("");
                    }
                } else {
                    showAlert("Ошибка при удалении таблицы.");
                }
            }
        } else {
            showAlert("Выберите таблицу для удаления.");
        }
    }

    @FXML
    private void handleSaveAs(ActionEvent event) {
        String selectedTable = tableController.tableListView.getSelectionModel().getSelectedItem();

        if (selectedTable != null) {
            List<String> choices = Arrays.asList("txt", "csv", "iqdat");
            ChoiceDialog<String> formatDialog = new ChoiceDialog<>(choices.get(2), choices);
            formatDialog.setTitle("Выбор формата сохранения");
            formatDialog.setHeaderText("Выберите формат для сохранения таблицы:");
            formatDialog.setContentText("Формат:");

            Optional<String> formatResult = formatDialog.showAndWait();
            String selectedFormat = formatResult.orElse("");

            if (!selectedFormat.isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Сохранить таблицу как");
                fileChooser.setInitialFileName(selectedTable + "." + selectedFormat);
                File file = fileChooser.showSaveDialog(null);

                if (file != null) {
                    boolean success = DatabaseHandler.saveTableAs(tableController.schemaComboBox.getValue(), selectedTable, file, selectedFormat);
                    if (success) {
                        showAlert("Таблица успешно сохранена.");
                    } else {
                        showAlert("Ошибка при сохранении таблицы.");
                    }
                }
            }
        } else {
            showAlert("Выберите таблицу для сохранения.");
        }
    }

    @FXML
    private void handleOpenTables(ActionEvent event) {
        Parent tableContent = loadFXML("table");
        getBorderPane().setCenter(tableContent);

        DatabaseSchema selectedSchema = tableController.schemaComboBox.getValue();
        tableController.allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables(selectedSchema));

        tableController.showTableList(tableController.searchField.getText());

        closeMenuItem.setVisible(true);

        tableController.tableListView.setOnMouseClicked(tableListEvent -> {
            if (tableListEvent.getClickCount() == 2) {
                tableController.handleTableDoubleClick();
            }
        });

        tableController.tableView.setVisible(false);
        tableController.tableNameLabel.setVisible(false);
        tableController.tableView.getColumns().clear();
    }



    @FXML
    private void handleClose() {
        tableMenu.setVisible(false);
        Parent mainScreenContent = loadFXML("mainScreen");
        closeMenuItem.setVisible(false);
        getBorderPane().setCenter(mainScreenContent);
    }


    public Parent loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nsu/fit/khomchenko/stopsignalmodule/" + fxmlPath + ".fxml"));
            Parent parent = loader.load();

            switch (fxmlPath) {
                case "mainScreen" -> {
                    mainScreenController = loader.getController();
                    mainScreenController.setMainController(this);
                }
                case "table" -> {
                    tableController = loader.getController();
                    tableController.setMainController(this);
                }
                case "statistics" -> {
                    statisticsController = loader.getController();
                    statisticsController.setMainController(this);
                }
            }

            borderPane.setCenter(parent);
            return parent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void calculateAndCreateStatistics(String schemaName) {
        switch (schemaName) {
            case "hunt":
                List<String> tableNames = DatabaseHandler.getTableNamesForSchema(schemaName);
                for (String tableName : tableNames) {
                    if (!tableName.equals("summary_table")) {
                        List<HuntData> huntDataList = DatabaseHandler.getHuntDataForTable(schemaName, tableName);
                        if (!huntDataList.isEmpty()) {
                            String statisticsResult = HuntStatisticsCalculator.calculateStatistics(huntDataList, tableName, schemaName);
                            System.out.println(statisticsResult);
                        } else {
                            System.out.println("Нет данных для таблицы " + tableName + " в схеме " + schemaName);
                        }
                    }
                }
                break;
        /*case "odd_ball_easy":
            List<OddBallEasyData> oddBallEasyDataList = DatabaseHandler.getOddBallEasyDataForSchema(schemaName);
            if (!oddBallEasyDataList.isEmpty()) {
                String statisticsResult = OddBallEasyStatisticsCalculator.calculateStatistics(oddBallEasyDataList);
                // В этом месте вы можете делать что-то с результатом статистики для схемы "odd_ball_easy"
            } else {
                System.out.println("Нет данных для схемы 'odd_ball_easy'");
            }
            break;
        case "odd_ball_hard":
            List<OddBallHardData> oddBallHardDataList = DatabaseHandler.getOddBallHardDataForSchema(schemaName);
            if (!oddBallHardDataList.isEmpty()) {
                String statisticsResult = OddBallHardStatisticsCalculator.calculateStatistics(oddBallHardDataList);
                // В этом месте вы можете делать что-то с результатом статистики для схемы "odd_ball_hard"
            } else {
                System.out.println("Нет данных для схемы 'odd_ball_hard'");
            }
            break;*/
            default:
                System.out.println("Неизвестная схема: " + schemaName);
                break;
        }
    }

    public void initializeStatistic() {
        calculateAndCreateStatistics("hunt");
        /*calculateAndCreateStatistics("odd_ball_easy");
        calculateAndCreateStatistics("odd_ball_hard");
        calculateAndCreateStatistics("stroop");*/
    }

    @FXML
    private void initialize() {
        mainController = this;
        initializeStatistic();
    }
}