package nsu.fit.khomchenko.stopsignalmodule;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    private static final boolean USE_CONSOLE_INTERFACE = false;
    private Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @FXML
    private ListView<String> tableListView;

    @FXML
    private Pagination pagination;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<String[]> tableView;
    private ObservableList<String> allTables;

    @FXML
    private Label tableNameLabel;

    private final int itemsPerPage = 10;

    @FXML
    private MenuBar menuBar;
    public Menu tableMenu;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem openTablesMenuItem;

    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
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
        if (scene != null) {
            String stylesheet = Objects.requireNonNull(getClass().getResource("/styles/" + theme + ".css")).toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(stylesheet);
        }
    }

    @FXML
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О приложении");
        alert.setHeaderText(null);
        alert.setContentText("Powered by Khomcha");

        alert.showAndWait();
    }

    @FXML
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Текстовые файлы (*.iqdat)", "*.iqdat");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (USE_CONSOLE_INTERFACE) {
                handleConsoleInterface(selectedFile);
            } else {
                handleDialogInterface(selectedFile);
                allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables());
                initializePagination();
            }
        }
    }

    private void handleConsoleInterface(File selectedFile) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите базовое название таблицы: ");
        String baseTableName = scanner.nextLine().trim();

        System.out.print("Выберите пол (М/Ж): ");
        String gender = scanner.nextLine().trim().toUpperCase();

        System.out.print("Введите возраст (0-120): ");
        int age = scanner.nextInt();

        if (baseTableName.isEmpty() || !(gender.equals("М") || gender.equals("Ж")) || age < 0 || age > 120) {
            System.out.println("Некорректный ввод. Введите правильные данные.");
            return;
        }

        String tableName = baseTableName + "_" + gender + "_" + age;

        String filePath = selectedFile.getAbsolutePath();
        DatabaseHandler.loadAndSaveData(filePath, tableName);
    }

    private void handleDialogInterface(File selectedFile) {
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

        String filePath = selectedFile.getAbsolutePath();
        DatabaseHandler.loadAndSaveData(filePath, tableName);
    }

    private void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Некорректный ввод");
            alert.setContentText("Введите правильные данные.");
            alert.showAndWait();
        });
    }

    private void showTableList(int pageIndex, String searchQuery) {
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, allTables.size());

        List<String> displayedTables = allTables.subList(fromIndex, toIndex);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            displayedTables = displayedTables.stream()
                    .filter(tableName -> tableName.toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        tableListView.setItems(FXCollections.observableArrayList(displayedTables));
    }

    private void initializePagination() {
        if (allTables != null && tableListView != null) {
            int pageCount = (int) Math.ceil((double) allTables.size() / itemsPerPage);
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(pageIndex -> {
                showTableList(pageIndex, searchField.getText());
                return tableListView;
            });
        }
    }


    @FXML
    private void handleSearch() {
        initializePagination();
    }

    @FXML
    private void handleOpenTables(ActionEvent event) {
        tableListView.setVisible(true);
        pagination.setVisible(true);
        searchField.setVisible(true);

        allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables());

        if (pagination != null) {
            initializePagination();
        }

        closeMenuItem.setVisible(true);
    }


    @FXML
    private void initialize() {
        tableListView.setVisible(false);
        tableView.setVisible(false);
        tableNameLabel.setVisible(false);
        pagination.setVisible(false);
        searchField.setVisible(false);
        tableListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleTableDoubleClick();
            }
        });

        closeMenuItem.setOnAction(event -> handleClose());
    }

    private void updateOpenTablesMenuItemVisibility() {
        boolean isTableListVisible = tableListView.isVisible();
        openTablesMenuItem.setVisible(!isTableListVisible);
    }


    @FXML
    private void handleTableDoubleClick() {
        String selectedTable = tableListView.getSelectionModel().getSelectedItem();

        if (selectedTable != null) {
            System.out.println("Выбрана таблица: " + selectedTable);

            List<String[]> tableData = DatabaseHandler.getDataForTable(selectedTable);

            List<String> columnNames = DatabaseHandler.getColumnNames(selectedTable);
            tableView.getColumns().clear();
            for (int i = 0; i < columnNames.size(); i++) {
                final int columnIndex = i;
                TableColumn<String[], String> column = new TableColumn<>(columnNames.get(i));
                column.setCellValueFactory(cellData -> {
                    String[] row = cellData.getValue();
                    return new SimpleStringProperty(row[columnIndex]);
                });
                tableView.getColumns().add(column);
            }

            tableView.getItems().clear();
            tableView.getItems().addAll(tableData);
            tableView.setVisible(true);

            tableNameLabel.setText("Выбрана таблица: " + selectedTable);
            tableNameLabel.setVisible(true);

            Menu tableMenu = menuBar.getMenus().get(2);
            tableMenu.setVisible(true);
        } else {
            tableView.setVisible(false);
            tableNameLabel.setVisible(false);

            Menu tableMenu = menuBar.getMenus().get(2);
            tableMenu.setVisible(false);
        }
    }

    @FXML
    private void handleDeleteTable() {
        String selectedTable = tableListView.getSelectionModel().getSelectedItem();

        if (selectedTable != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить таблицу?");
            alert.setContentText("Таблица: " + selectedTable);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = DatabaseHandler.deleteTable(selectedTable);
                if (success) {
                    showAlert("Таблица успешно удалена.");

                    allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables());

                    if (allTables.isEmpty()) {
                        tableListView.setVisible(false);
                        pagination.setVisible(false);
                        searchField.setVisible(false);

                        tableView.setVisible(false);
                        tableNameLabel.setVisible(false);

                        Menu tableMenu = menuBar.getMenus().get(2);
                        tableMenu.setVisible(false);
                    } else {
                        initializePagination();

                        tableView.getItems().clear();
                        tableNameLabel.setText("");
                    }
                } else {
                    showAlert("Ошибка при удалении таблицы.");
                }
            }
        } else {
            showAlert("Выберите таблицу для удаления.");
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
    private void handleSaveAs(ActionEvent event) {
        String selectedTable = tableListView.getSelectionModel().getSelectedItem();

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
                    boolean success = DatabaseHandler.saveTableAs(selectedTable, file, selectedFormat);
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
    private void handleClose() {
        tableListView.setVisible(false);
        tableView.setVisible(false);
        tableNameLabel.setVisible(false);
        pagination.setVisible(false);
        searchField.setVisible(false);
        closeMenuItem.setVisible(false);
        tableMenu.setVisible(false);
    }

}