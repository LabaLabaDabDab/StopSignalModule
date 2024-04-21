package nsu.fit.khomchenko.stopsignalmodule;

import javafx.scene.control.Alert;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {
    public static String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
    private static String username = "postgres";
    private static String password = "1";

    public static void setJdbcUrl(String newJdbcUrl) {
        jdbcUrl = newJdbcUrl;
    }

    public static void setUsername(String newUsername) {
        username = newUsername;
    }

    public static void setPassword(String newPassword) {
        password = newPassword;
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() {
        try {
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the database: " + e.getMessage());
            showErrorAlert(e.getMessage(), e.getMessage());
            return null;
        }
    }

    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Connection connect(String schemaName) {
        createSchema(schemaName);
        try {
            String jdbcUrlNew = jdbcUrl + "?currentSchema=" + schemaName;
            return DriverManager.getConnection(jdbcUrlNew, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the database: " + e.getMessage());
            showErrorAlert("Failed to connect to the database", e.getMessage());
            return null;
        }
    }

    public static void createSchema(String schemaName) {
        try (Connection connection = connect()) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + schemaName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Exception while creating schema: " + e.getMessage());
        }
    }

    public static void loadAndSaveData(String filePath, String tableName, String schemaName) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                createTable(tableName, headerLine, schemaName);

                String line;
                while ((line = br.readLine()) != null) {
                    saveDataRow(tableName, line, schemaName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createTable(String tableName, String headerLine, String schemaName) {
        try (Connection connection = connect(schemaName)) {
            assert connection != null;
            try (Statement statement = connection.createStatement()) {
                String[] columns = headerLine.replace(".", "_").split("\t");

                StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                createTableQuery.append(schemaName).append(".").append(tableName).append(" (");
                for (String column : columns) {
                    createTableQuery.append(column).append(" VARCHAR(255), ");
                }
                createTableQuery.delete(createTableQuery.length() - 2, createTableQuery.length());
                createTableQuery.append(")");

                statement.executeUpdate(createTableQuery.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Exception while executing statement: " + e.getMessage());
        }
    }


    private static void saveDataRow(String tableName, String dataRow, String schemaName) {
        try (Connection connection = connect(schemaName)) {
            if (connection != null) {
                try {
                    String[] values = dataRow.split("\t");

                    StringBuilder sql = new StringBuilder("INSERT INTO " + schemaName + "." + tableName + " VALUES (");
                    for (int i = 0; i < values.length; i++) {
                        sql.append("?, ");
                    }
                    sql.delete(sql.length() - 2, sql.length());
                    sql.append(")");

                    try (PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
                        for (int i = 0; i < values.length; i++) {
                            preparedStatement.setString(i + 1, values[i]);
                        }
                        preparedStatement.executeUpdate();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllTables(DatabaseSchema schema) {
        List<String> tableNames = new ArrayList<>();

        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                DatabaseMetaData metaData = connection.getMetaData();

                ResultSet resultSet = metaData.getTables(null, schema.getSchemaName(), "%", new String[]{"TABLE"});

                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    tableNames.add(tableName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableNames;
    }

    public static List<String[]> getDataForTable(DatabaseSchema schema, String tableName) {
        List<String[]> tableData = new ArrayList<>();

        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);

                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (resultSet.next()) {
                        String[] row = new String[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = resultSet.getString(i);
                        }
                        tableData.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Exception while executing statement: " + e.getMessage());
        }

        return tableData;
    }

    public static List<String> getColumnNames(DatabaseSchema schema, String tableName) {
        List<String> columnNames = new ArrayList<>();

        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                DatabaseMetaData metaData = connection.getMetaData();

                ResultSet resultSet = metaData.getColumns(null, schema.getSchemaName(), tableName, null);

                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    columnNames.add(columnName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columnNames;
    }

    public static boolean deleteTable(DatabaseSchema schema, String tableName) {
        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
                    updateSummaryTableOnTableDeletion(schema, tableName);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static List<String> getTableNames(Connection connection, String schemaName) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, schemaName, null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("TABLE_NAME"));
            }
        }
        return tableNames;
    }


    public static boolean saveTableAs(DatabaseSchema schema, String tableName, File file, String format) {
        List<String[]> tableData = getDataForTable(schema, tableName);
        List<String> columnNames = getColumnNames(schema, tableName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            switch (format.toLowerCase()) {
                case "txt" -> saveAsTxt(tableData, columnNames, writer);
                case "csv" -> saveAsCsv(tableData, columnNames, writer);
                case "iqdat" -> saveAsIqdat(tableData, columnNames, writer);
                default -> {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void saveAsTxt(List<String[]> tableData, List<String> columnNames, BufferedWriter writer) throws IOException {
        writer.write(String.join("\t", columnNames));
        writer.newLine();

        for (String[] row : tableData) {
            writer.write(String.join("\t", row));
            writer.newLine();
        }
    }

    private static void saveAsCsv(List<String[]> tableData, List<String> columnNames, BufferedWriter writer) throws IOException {
        writer.write(String.join(";", columnNames));
        writer.newLine();

        for (String[] row : tableData) {
            writer.write(String.join(";", row));
            writer.newLine();
        }
    }

    private static void saveAsIqdat(List<String[]> tableData, List<String> columnNames, BufferedWriter writer) throws IOException {
        writer.write(String.join("\t", columnNames));
        writer.newLine();

        for (String[] row : tableData) {
            writer.write(String.join("\t", row));
            writer.newLine();
        }
    }

    public static boolean isTableExists(String tableName, Connection connection, DatabaseSchema schema) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, schema.getSchemaName(), tableName, null)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Exception while checking if table exists: " + e.getMessage());
            return false;
        }
    }

    public static void saveStatisticsToSummaryTable(DatabaseSchema schema, String tableNameSource,
                                                    List<String> columnNames, List<Double> statistics) {
        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                if (!isTableExists("summary_table", connection, schema)) {
                    createSummaryTable("summary_table", connection, schema, columnNames);
                }

                if (isRecordExists(tableNameSource, connection, schema)) {
                    updateSummaryTable(tableNameSource, connection, schema, columnNames, statistics);
                } else {
                    insertSummaryTable(tableNameSource, connection, schema, columnNames, statistics);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Exception while executing statement: " + e.getMessage());
        }
    }

    private static void createSummaryTable(String tableName, Connection connection, DatabaseSchema schema, List<String> columnNames) throws SQLException {
        StringBuilder createStatement = new StringBuilder();
        createStatement.append("CREATE TABLE ").append(schema.getSchemaName()).append(".").append(tableName).append(" (");
        createStatement.append("source_table_name VARCHAR(255), ");

        for (int i = 0; i < columnNames.size(); i++) {
            createStatement.append(columnNames.get(i)).append(" VARCHAR(255)");
            if (i < columnNames.size() - 1) {
                createStatement.append(", ");
            }
        }
        createStatement.append(")");

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createStatement.toString());
        }
    }


    private static boolean isRecordExists(String tableNameSource, Connection connection, DatabaseSchema schema) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + schema.getSchemaName() + "." + "summary_table" + " WHERE source_table_name = ?")) {
            statement.setString(1, tableNameSource);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void updateSummaryTable(String tableNameSource, Connection connection, DatabaseSchema schema,
                                           List<String> columnNames, List<Double> statistics) throws SQLException {
        StringBuilder updateStatement = new StringBuilder();
        updateStatement.append("UPDATE ").append(schema.getSchemaName()).append(".").append("summary_table").append(" SET ");

        for (int i = 0; i < columnNames.size(); i++) {
            updateStatement.append(columnNames.get(i)).append(" = ?");
            if (i < columnNames.size() - 1) {
                updateStatement.append(", ");
            }
        }
        updateStatement.append(" WHERE source_table_name = ?");

        try (PreparedStatement statement = connection.prepareStatement(updateStatement.toString())) {
            for (int i = 0; i < statistics.size(); i++) {
                statement.setString(i + 1, formatValue(statistics.get(i), columnNames.get(i)));
            }
            statement.setString(statistics.size() + 1, tableNameSource);
            statement.executeUpdate();
        }
    }

    private static void insertSummaryTable(String tableNameSource, Connection connection, DatabaseSchema schema,
                                           List<String> columnNames, List<Double> statistics) throws SQLException {
        StringBuilder insertStatement = new StringBuilder();
        insertStatement.append("INSERT INTO ").append(schema.getSchemaName()).append(".").append("summary_table").append(" (source_table_name");
        for (String columnName : columnNames) {
            insertStatement.append(", ").append(columnName);
        }
        insertStatement.append(") VALUES (?");
        for (int i = 0; i < columnNames.size(); i++) {
            insertStatement.append(", ?");
        }
        insertStatement.append(")");

        try (PreparedStatement statement = connection.prepareStatement(insertStatement.toString())) {
            statement.setString(1, tableNameSource);
            for (int i = 0; i < statistics.size(); i++) {
                statement.setString(i + 2, formatValue(statistics.get(i), columnNames.get(i)));
            }
            statement.executeUpdate();
        }
    }

    private static String formatValue(double value, String columnName) {
        return columnName.toLowerCase().contains("percentage") ? String.format("%.2f%%", value) : String.valueOf(value);
    }

    public static List<HuntData> getHuntDataForTable(DatabaseSchema schema, String tableName) {
        List<HuntData> dataList = new ArrayList<>();
        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + schema.getSchemaName() + "." + tableName + " WHERE trialcode != 'CRTTpractice'");
                    while (resultSet.next()) {
                        HuntData data = new HuntData();
                        data.setDate(resultSet.getString("date"));
                        data.setTime(resultSet.getString("time"));
                        data.setSubject(resultSet.getString("subject"));
                        data.setBlockcode(resultSet.getString("blockcode"));
                        data.setTrialcode(resultSet.getString("trialcode"));
                        data.setPicture_target_currentitem(resultSet.getString("picture_target_currentitem"));
                        data.setValues_stiminterval(resultSet.getString("values_stiminterval"));
                        data.setLatency(resultSet.getString("latency"));
                        data.setResponse(resultSet.getString("response"));
                        data.setCorrect(resultSet.getString("correct"));
                        data.setValues_score(resultSet.getString("values_score"));
                        data.setValues_averagertpractice(resultSet.getString("values_averagertpractice"));
                        data.setValues_ssdcoeffitient(resultSet.getString("values_ssdcoeffitient"));
                        data.setValues_ssd(resultSet.getString("values_ssd"));
                        dataList.add(data);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public static List<OddBallData> getOddBallDataForSchema(DatabaseSchema schema, String tableName) {
        List<OddBallData> dataList = new ArrayList<>();
        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + schema.getSchemaName() + "." + tableName +  " WHERE trialcode != 'CRTTpractice'");
                    while (resultSet.next()) {
                        OddBallData data = new OddBallData();
                        data.setDate(resultSet.getString("date"));
                        data.setTime(resultSet.getString("time"));
                        data.setSubject(resultSet.getString("subject"));
                        data.setTrialcode(resultSet.getString("trialcode"));
                        data.setTrialnum(resultSet.getString("trialnum"));
                        data.setResponse(resultSet.getString("response"));
                        data.setLatency(resultSet.getString("latency"));
                        data.setCorrect(resultSet.getString("correct"));
                        data.setValues_lasttrial(resultSet.getString("values_lasttrial"));
                        data.setValues_skipcount(resultSet.getString("values_skipcount"));
                        dataList.add(data);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public static void updateSummaryTableOnTableDeletion(DatabaseSchema schema, String tableName) {
        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM " + schema.getSchemaName() + ".summary_table WHERE source_table_name = ?")) {
                    statement.setString(1, tableName);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> getAverageStatistics(DatabaseSchema schema, boolean isMaleSelected, boolean isFemaleSelected, int ageLowerBound, int ageUpperBound) {
        Map<String, Double> averageValuesMap = new HashMap<>();

        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                String query = "SELECT ";

                List<String> columnNames = getColumnNames(schema, "summary_table");
                for (String columnName : columnNames) {
                    if (!columnName.equals("source_table_name")) {
                        query += "AVG(CAST(REPLACE(REPLACE(" + columnName + ", '%', ''), ',', '.') AS DECIMAL(10,2))) as " + columnName + ", ";
                    }
                }
                query = query.substring(0, query.length() - 2);
                query += " FROM summary_table";

                query += " WHERE CAST(REPLACE(SPLIT_PART(source_table_name, '_', 3), '%', '') AS DECIMAL(10,0)) BETWEEN ? AND ?";

                if (isMaleSelected && isFemaleSelected) {
                    query += " AND (source_table_name LIKE '%_М_%' OR source_table_name LIKE '%_Ж_%')";
                } else if (!isMaleSelected && !isFemaleSelected) {
                    query += " AND NOT (source_table_name LIKE '%_М_%' OR source_table_name LIKE '%_Ж_%')";
                } else if (isMaleSelected) {
                    query += " AND source_table_name LIKE '%_М_%'";
                } else if (isFemaleSelected) {
                    query += " AND source_table_name LIKE '%_Ж_%'";
                }

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, ageLowerBound);
                    statement.setInt(2, ageUpperBound);
                    ResultSet resultSet = statement.executeQuery();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    if (resultSet.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            double columnAverage = resultSet.getDouble(i);
                            averageValuesMap.put(columnName, columnAverage);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return averageValuesMap;
    }
}