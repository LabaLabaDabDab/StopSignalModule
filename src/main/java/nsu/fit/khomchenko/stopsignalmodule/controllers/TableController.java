package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableController {

    @FXML
    public ListView<String> tableListView;

    @FXML
    public TextField searchField;

    @FXML
    public Label schemaLabel;

    @FXML
    public TableView<String[]> tableView;
    public ObservableList<String> allTables;

    @FXML
    public Label tableNameLabel;

    @FXML
    public ComboBox<DatabaseSchema> schemaComboBox;

    @FXML
    public MainScreenController mainScreenController;

    @FXML
    public MainController mainController;

    @FXML
    public TableController tableController;

    @FXML
    public VBox VboxForList;

    @FXML
    public Label testSubjectsLabel;
    @FXML
    public Button statisticButton;
    @FXML
    public ImageView refreshButton;
    @FXML
    public Button statisticUnhealthyButton;

    @FXML
    public void handleSearch(KeyEvent event) {
        showTableList(searchField.getText());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void showTableList(String searchQuery) {
        List<String> displayedTables = allTables;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            displayedTables = displayedTables.stream()
                    .filter(tableName -> tableName.toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        displayedTables = displayedTables.stream()
                .filter(tableName -> !tableName.equals("summary_table"))
                .filter(tableName -> !tableName.equals("summary_table_unhealthy"))
                .collect(Collectors.toList());

        tableListView.setItems(FXCollections.observableArrayList(displayedTables));
    }


    @FXML
    public void handleTableDoubleClick() {
        String selectedTable = tableListView.getSelectionModel().getSelectedItem();

        if (selectedTable != null) {
            System.out.println("Выбрана таблица: " + selectedTable);

            List<String[]> tableData = DatabaseHandler.getDataForTable(schemaComboBox.getValue(), selectedTable);

            List<String> columnNames = DatabaseHandler.getColumnNames(schemaComboBox.getValue(), selectedTable);
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

            tableNameLabel.setText("Выбран испытуемый: " + selectedTable);
            tableNameLabel.setVisible(true);

            Menu tableMenu = mainController.menuBar.getMenus().get(2);
            tableMenu.setVisible(true);

            refreshButton.setVisible(true);
        } else {
            tableView.setVisible(false);
            tableNameLabel.setVisible(false);

            Menu tableMenu = mainController.menuBar.getMenus().get(2);
            tableMenu.setVisible(false);
        }
    }

    @FXML
    public void handleSchemaSelection() {
        DatabaseSchema selectedSchema = schemaComboBox.getValue();
        if (selectedSchema != null) {
            allTables = FXCollections.observableArrayList(DatabaseHandler.getAllTables(selectedSchema));
            showTableList(searchField.getText());
        }
    }

    private void displayTableData(DatabaseSchema schema, String tableName) {
        if (schema != null && tableName != null) {
            List<String[]> tableData = DatabaseHandler.getDataForTable(schema, tableName);

            List<String> columnNames = DatabaseHandler.getColumnNames(schema, tableName);
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

            if (tableName.equals("summary_table")) {
                tableNameLabel.setText("Общая статистика здоровых испытуемых для : " + schema);
            } else if (tableName.equals("summary_table_unhealthy")) {
                tableNameLabel.setText("Общая статистика не здоровых испытуемых для : " + schema);
            } else {
                tableNameLabel.setText("Выбран испытуемый: " + tableName);
            }

            tableNameLabel.setVisible(true);

            Menu tableMenu = mainController.menuBar.getMenus().get(2);
            tableMenu.setVisible(true);
        } else {
            tableView.setVisible(false);
            tableNameLabel.setVisible(false);
        }
    }

    @FXML
    public void showSummaryTable(String statisticTableName) {
        DatabaseSchema selectedSchema = schemaComboBox.getValue();
        displayTableData(selectedSchema, statisticTableName);
        refreshButton.setVisible(true);

        Menu tableMenu = mainController.menuBar.getMenus().get(2);
        tableMenu.setVisible(false);
    }

    @FXML
    public void handleRefreshButtonHover(MouseEvent event) {
        refreshButton.setEffect(new DropShadow(10.0, Color.BLACK));
    }

    @FXML
    public void handleRefreshButtonExit(MouseEvent event) {
        refreshButton.setEffect(null);
    }

    @FXML
    public void handleRefreshButtonClick(MouseEvent event) {
        refreshButton.setEffect(null);
        DatabaseSchema selectedSchema = schemaComboBox.getValue();
        String selectedTable = tableListView.getSelectionModel().getSelectedItem();
        if (selectedTable != null) {
            displayTableData(selectedSchema, selectedTable);
        } else {
            showSummaryTable("summary_table");
        }
    }

    @FXML
    private void initialize() {
        List<DatabaseSchema> schemaList = Arrays.asList(DatabaseSchema.values());
        refreshButton.setVisible(false);

        URL imageUrl = getClass().getResource("/icons/refresh_icon.png");
        if (imageUrl != null) {
            Image image = new Image(imageUrl.toExternalForm());
            refreshButton.setImage(image);
        } else {
            System.err.println("Не удалось загрузить изображение");
        }

        schemaComboBox.getItems().addAll(schemaList);

        schemaComboBox.setValue(DatabaseSchema.HUNT);
        schemaComboBox.setOnAction(event -> handleSchemaSelection());

        statisticButton.setOnAction(event -> showSummaryTable("summary_table"));
        statisticUnhealthyButton.setOnAction(event -> showSummaryTable("summary_table_unhealthy"));

        tableListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleTableDoubleClick();
            }
        });
    }
}