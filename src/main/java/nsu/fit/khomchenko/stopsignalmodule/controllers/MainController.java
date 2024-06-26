package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;
import nsu.fit.khomchenko.stopsignalmodule.data.StroopData;
import nsu.fit.khomchenko.stopsignalmodule.data.DataBaseSettings;
import nsu.fit.khomchenko.stopsignalmodule.utils.HuntStatisticsCalculator;
import nsu.fit.khomchenko.stopsignalmodule.utils.OddBallStatisticsCalculator;
import nsu.fit.khomchenko.stopsignalmodule.utils.StroopStatisticsCalculator;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static nsu.fit.khomchenko.stopsignalmodule.utils.InputDialogHelper.*;


public class MainController {
    @FXML
    public Menu tableMenu;
    @FXML
    public MenuItem closeMenuItem;
    @FXML
    public MenuItem openTablesMenuItem;
    @FXML
    public Menu closeMenu;
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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    private StatisticsController statisticsController;

    public void setScene(Scene scene) {
        this.scene = scene;
    }



    public BorderPane getBorderPane() {
        return borderPane;
    }


    public StatisticsController getStatisticsController() {
        return statisticsController;
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
        alert.setHeaderText("Модуль тестирования поведенческих данных для программы тестирования моторного контроля");
        alert.getDialogPane().setStyle("-fx-font-size: 16px;");

        Label contentLabel = new Label(
                "Этот модуль тестирования разработан Хомченко Станиславом Евгеньевичем, ФИТ НГУ.\n" +
                        "Модуль включает в себя следующие методики: стоп-сигнал, Odd-ball, тест Струпа.\n" +
                        "Приложение написано на Java версии 21 с использованием PostgresSQL.\n" +
                        "Для связи по вопросам и предложениям вы можете обратиться в Telegram:");

        contentLabel.setStyle("-fx-font-size: 16px;");

        Hyperlink hyperlink = new Hyperlink("https://t.me/LabaLabaDabDab");
        hyperlink.setStyle("-fx-font-size: 16px;");
        hyperlink.setOnAction(e -> {
            openWebpage(hyperlink.getText());
        });

        VBox vbox = new VBox(contentLabel, hyperlink);
        vbox.setSpacing(10);

        alert.getDialogPane().setContent(vbox);
        alert.showAndWait();
    }

    private void openWebpage(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Текстовые файлы (*.iqdat)", "*.iqdat");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            DatabaseSchema selectedSchema = showSchemaSelectionDialog();
            if (selectedSchema != null) {
                handleDialogInterface(selectedFile, selectedSchema);

                updateTableList(selectedSchema);
            }
        }
    }

    public void handleDialogInterface(File selectedFile, DatabaseSchema selectedSchema) {
        Optional<String> testPerson = promptTestPersonName();
        Optional<String> genderResult = promptGender();
        Optional<Integer> ageResult = promptAge();

        if (testPerson.isPresent() && genderResult.isPresent() && ageResult.isPresent()) {
            String baseTableName = testPerson.get();
            String gender = genderResult.get();
            int age = ageResult.get();

            if (!Arrays.asList("М", "Ж").contains(gender) || age < 0 || age > 120) {
                showAlert();
                return;
            }

            String tableName = baseTableName + "_" + gender + "_" + age;
            String schemaName = selectedSchema.getSchemaName();

            String filePath = selectedFile.getAbsolutePath();
            executor.submit(() -> {
                DatabaseHandler.loadAndSaveData(filePath, tableName, schemaName);
                Platform.runLater(() -> {
                    calculateAndCreateStatistics(selectedSchema);
                    updateTableList(selectedSchema);
                });
            });
        } else {
            showAlert();
        }
    }

    private void updateTableList(DatabaseSchema selectedSchema) {
        if (tableController != null) {
            tableController.allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables(selectedSchema));
            tableController.showTableList(tableController.searchField.getText());
        }
    }

    public void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Некорректный ввод");
            alert.setContentText("Введите правильные данные.");
            alert.showAndWait();
        });
    }

    public static void showAlert(String message) {
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
    public void handleOpenTables(ActionEvent event) {
        Parent tableContent = loadFXML("table");
        getBorderPane().setCenter(tableContent);

        DatabaseSchema selectedSchema = tableController.schemaComboBox.getValue();
        tableController.allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables(selectedSchema));

        tableController.showTableList(tableController.searchField.getText());


        closeMenu.setVisible(true);
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
        closeMenu.setVisible(false);
        tableMenu.setVisible(false);
        Parent mainScreenContent = loadFXML("mainScreen");
        closeMenuItem.setVisible(false);
        getBorderPane().setCenter(mainScreenContent);
    }

    public void switchToStatistic(){
        Parent mainScreenContent = loadFXML("statistics");
        closeMenu.setVisible(true);
        closeMenuItem.setVisible(true);
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

    private Map<String, Map<String, String>> processStatistics(DatabaseSchema schema, String tableName) {
        Map<String, Map<String, String>> statisticsResult = new HashMap<>();

        switch (schema) {
            case HUNT -> {
                List<HuntData> huntDataList = DatabaseHandler.getHuntDataForTable(schema, tableName);
                if (!huntDataList.isEmpty()) {
                    statisticsResult = HuntStatisticsCalculator.calculateStatistics(huntDataList, tableName, schema, true);
                } else {
                    System.out.println("Нет данных для таблицы " + tableName + " в схеме HUNT");
                }
            }
            case ODD_BALL_EASY, ODD_BALL_HARD -> {
                List<OddBallData> oddBallDataList = DatabaseHandler.getOddBallDataForSchema(schema, tableName);
                if (!oddBallDataList.isEmpty()) {
                    statisticsResult = OddBallStatisticsCalculator.calculateStatistics(oddBallDataList, tableName, schema, true);
                } else {
                    System.out.println("Нет данных для таблицы " + tableName + " в схеме " + schema.getSchemaName());
                }
            }
            case STROOP -> {
                List<StroopData> stroopDataList = DatabaseHandler.getStroopDataForSchema(schema, tableName);
                if (!stroopDataList.isEmpty()) {
                    statisticsResult = StroopStatisticsCalculator.calculateStatistics(stroopDataList, tableName, schema, true);
                } else {
                    System.out.println("Нет данных для таблицы " + tableName + " в схеме " + schema.getSchemaName());
                }
            }
            default -> System.out.println("Неизвестная схема: " + schema.getSchemaName());
        }

        return statisticsResult;
    }

    public List<Double> calculateStatisticsForTable(DatabaseSchema schema, String tableName) {
        List<Double> statistics = new ArrayList<>();
        Map<String, Map<String, String>> statisticsResult = processStatistics(schema, tableName);
        return statistics;
    }

    public void calculateAndCreateStatistics(DatabaseSchema schema) {
        List<String> tableNames = DatabaseHandler.getAllTables(schema);
        if (tableNames.isEmpty()) {
            System.out.println("Нет данных для схемы " + schema);
            return;
        }
        for (String tableName : tableNames) {
            if (tableName.equals("summary_table") || tableName.endsWith("_test") || tableName.equals("summary_table_unhealthy")) {
                continue;
            }
            processStatistics(schema, tableName);
        }
    }

    private void deleteTestTablesFromAllSchemas() {
        for (DatabaseSchema schema : DatabaseSchema.values()) {
            List<String> tableNames = DatabaseHandler.getAllTables(schema);

            List<String> testTableNames = tableNames.stream()
                    .filter(tableName -> tableName.endsWith("_test"))
                    .collect(Collectors.toList());

            testTableNames.forEach(tableName -> {
                boolean success = DatabaseHandler.deleteTable(schema, tableName);
                if (success) {
                    System.out.println("Таблица " + tableName + " из схемы " + schema.getSchemaName() + " успешно удалена.");
                } else {
                    System.out.println("Ошибка при удалении таблицы " + tableName + " из схемы " + schema.getSchemaName());
                }
            });
        }
    }

    public void handleDatabaseSettings(ActionEvent actionEvent) {
        Dialog<ArrayList<String>> dialog = new Dialog<>();
        dialog.setTitle("Настройки подключения к БД");
        dialog.setHeaderText("Введите данные для подключения к базе данных:");

        TextField hostField = new TextField();
        hostField.setPromptText("Хост");
        TextField portField = new TextField();
        portField.setPromptText("Порт");
        TextField databaseField = new TextField();
        databaseField.setPromptText("Имя базы данных");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Имя пользователя");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        hostField.setText(prefs.get("host", ""));
        portField.setText(prefs.get("port", ""));
        databaseField.setText(prefs.get("databaseName", ""));
        usernameField.setText(prefs.get("username", ""));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));
        gridPane.add(new Label("Хост:"), 0, 0);
        gridPane.add(hostField, 1, 0);
        gridPane.add(new Label("Порт:"), 0, 1);
        gridPane.add(portField, 1, 1);
        gridPane.add(new Label("Имя базы данных:"), 0, 2);
        gridPane.add(databaseField, 1, 2);
        gridPane.add(new Label("Имя пользователя:"), 0, 3);
        gridPane.add(usernameField, 1, 3);
        gridPane.add(new Label("Пароль:"), 0, 4);
        gridPane.add(passwordField, 1, 4);
        dialog.getDialogPane().setContent(gridPane);

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                ArrayList<String> data = new ArrayList<>();
                data.add(hostField.getText());
                data.add(portField.getText());
                data.add(databaseField.getText());
                data.add(passwordField.getText());
                return data;
            }
            return null;
        });

        Optional<ArrayList<String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String enteredHost = data.get(0);
            String enteredPort = data.get(1);
            String enteredDatabase = data.get(2);
            String enteredUser = usernameField.getText();
            String enteredPassword = data.get(3);

            System.out.println("Host: " + enteredHost);
            System.out.println("Port: " + enteredPort);
            System.out.println("Database: " + enteredDatabase);
            System.out.println("User: " + enteredUser);
            System.out.println("Password: " + enteredPassword);

            try {
                String fullUrl = "jdbc:postgresql://" + enteredHost + ":" + enteredPort + "/" + enteredDatabase;
                Connection connection = DriverManager.getConnection(fullUrl, enteredUser, enteredPassword);
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Подключение к базе данных успешно");

                changeDatabaseSettings(fullUrl, enteredUser, enteredPassword, enteredHost, enteredPort, enteredDatabase);

                DataBaseSettings.setJdbcUrl(fullUrl);
                DataBaseSettings.setUsername(enteredUser);
                DataBaseSettings.setPassword(enteredPassword);
                DataBaseSettings.setHost(enteredHost);
                DataBaseSettings.setPort(enteredPort);
                DataBaseSettings.setDatabaseName(enteredDatabase);

                showAlert(Alert.AlertType.INFORMATION, "Успех", "Настройки успешно изменены");
                initializeStatistic();
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAndRetry("Не удалось подключиться к базе данных");
            }
        });
    }

    private void showErrorAndRetry(String errorMessage) {
        showErrorAlert(errorMessage);

        handleDatabaseSettings(null);

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, "Ошибка", message);
    }

    public void changeDatabaseSettings(String newJdbcUrl, String newUsername, String newPassword, String newHost, String newPort, String newDatabaseName) {
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        prefs.put("jdbcUrl", newJdbcUrl);
        prefs.put("username", newUsername);
        prefs.put("password", newPassword);
        prefs.put("host", newHost);
        prefs.put("port", newPort);
        prefs.put("databaseName", newDatabaseName);
    }

    private void loadDatabaseSettings() {
        Preferences prefs = Preferences.userNodeForPackage(MainController.class);
        String jdbcUrl = prefs.get("jdbcUrl", "");
        String username = prefs.get("username", "");
        String password = prefs.get("password", "");
        String host = prefs.get("host", "");
        String port = prefs.get("port", "");
        String databaseName = prefs.get("databaseName", "");

        DataBaseSettings.setJdbcUrl(jdbcUrl);
        DataBaseSettings.setUsername(username);
        DataBaseSettings.setPassword(password);
        DataBaseSettings.setHost(host);
        DataBaseSettings.setPort(port);
        DataBaseSettings.setDatabaseName(databaseName);
    }


    public void initializeStatistic() {
        calculateAndCreateStatistics(DatabaseSchema.HUNT);
        calculateAndCreateStatistics(DatabaseSchema.ODD_BALL_EASY);
        calculateAndCreateStatistics(DatabaseSchema.ODD_BALL_HARD);
        calculateAndCreateStatistics(DatabaseSchema.STROOP);
    }

    private boolean isDatabaseConnected() {
        Connection connection = DatabaseHandler.connect();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    @FXML
    private void initialize() {
        mainController = this;
        loadFXML("mainScreen");

        loadDatabaseSettings();

        if (!isDatabaseConnected()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Отсутствует подключение к базе данных. Пожалуйста, введите данные для подключения.");
        } else {
            initializeStatistic();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::deleteTestTablesFromAllSchemas));
    }
}