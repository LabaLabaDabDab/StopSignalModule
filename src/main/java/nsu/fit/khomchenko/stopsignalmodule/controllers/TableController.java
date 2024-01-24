package nsu.fit.khomchenko.stopsignalmodule.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    public MenuController menuController;

    @FXML
    public MainScreenController mainScreenController;

    @FXML
    public TableController tableController;

    @FXML
    public void handleSearch(KeyEvent event) {
        showTableList(searchField.getText());
    }



    public void showTableList(String searchQuery) {
        List<String> displayedTables = allTables;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            displayedTables = displayedTables.stream()
                    .filter(tableName -> tableName.toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

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

            tableNameLabel.setText("Выбрана таблица: " + selectedTable);
            tableNameLabel.setVisible(true);

            Menu tableMenu = menuController.menuBar.getMenus().get(2);
            tableMenu.setVisible(true);
        } else {
            tableView.setVisible(false);
            tableNameLabel.setVisible(false);

            Menu tableMenu = menuController.menuBar.getMenus().get(2);
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

}
