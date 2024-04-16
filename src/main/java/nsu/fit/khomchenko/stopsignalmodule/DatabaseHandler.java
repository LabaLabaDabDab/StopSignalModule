package nsu.fit.khomchenko.stopsignalmodule;

import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "1";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() {
        try {
            return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the database: " + e.getMessage());
            return null;
        }
    }

    public static Connection connect(String schemaName) {
        createSchema(schemaName);

        try {
            String jdbcUrl = JDBC_URL + "?currentSchema=" + schemaName;
            return DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the database: " + e.getMessage());
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
                                                    double successfulStopsPercentage, double missedPressesPercentage, double incorrectPressesPercentagePercentage,
                                                    double correctPressesPercentage, double averageLatencyForCorrectPresses,
                                                    double individualTimeDispersion) {
        try (Connection connection = connect(schema.getSchemaName())) {
            if (connection != null) {
                if (!isTableExists("summary_table", connection, schema)) {
                    createSummaryTable("summary_table", connection, schema);
                }

                if (isRecordExists(tableNameSource, connection, schema)) {
                    updateSummaryTable(tableNameSource, connection, schema,
                            successfulStopsPercentage, missedPressesPercentage, incorrectPressesPercentagePercentage,
                            correctPressesPercentage, averageLatencyForCorrectPresses,
                            individualTimeDispersion);
                } else {
                    insertSummaryTable(tableNameSource, connection, schema,
                            successfulStopsPercentage, missedPressesPercentage, incorrectPressesPercentagePercentage,
                            correctPressesPercentage, averageLatencyForCorrectPresses,
                            individualTimeDispersion);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Exception while executing statement: " + e.getMessage());
        }
    }

    private static void createSummaryTable(String tableName, Connection connection, DatabaseSchema schema) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE " +  schema.getSchemaName() + "." + tableName + " (" +
                    "source_table_name VARCHAR(255), " +
                    "successful_stops_percentage VARCHAR, " +
                    "missed_presses_percentage VARCHAR, " +
                    "incorrect_presses_percentage VARCHAR, " +
                    "correct_presses_percentage VARCHAR, " +
                    "average_latency_for_correct_presses VARCHAR, " +
                    "individual_time_dispersion VARCHAR)");
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
                                           double successfulStopsPercentage, double missedPressesPercentage,
                                           double incorrectPressesPercentage, double correctPressesPercentage,
                                           double averageLatencyForCorrectPresses, double individualTimeDispersion) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + schema.getSchemaName() + "." + "summary_table" + " SET " +
                        "successful_stops_percentage = ?, " +
                        "missed_presses_percentage = ?, " +
                        "incorrect_presses_percentage = ?, " +
                        "correct_presses_percentage = ?, " +
                        "average_latency_for_correct_presses = ?, " +
                        "individual_time_dispersion = ? " +
                        "WHERE source_table_name = ?")) {
            statement.setString(1, String.format("%.2f%%", successfulStopsPercentage));
            statement.setString(2, String.format("%.2f%%", missedPressesPercentage));
            statement.setString(3, String.format("%.2f%%", incorrectPressesPercentage));
            statement.setString(4, String.format("%.2f%%", correctPressesPercentage));
            statement.setString(5, String.format("%.5f", averageLatencyForCorrectPresses));
            statement.setString(6, String.format("%.5f", individualTimeDispersion));
            statement.setString(7, tableNameSource);
            statement.executeUpdate();
        }
    }

    private static void insertSummaryTable(String tableNameSource, Connection connection, DatabaseSchema schema,
                                           double successfulStopsPercentage, double missedPressesPercentage,
                                           double incorrectPressesPercentage, double correctPressesPercentage,
                                           double averageLatencyForCorrectPresses, double individualTimeDispersion) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + schema.getSchemaName() + "." + "summary_table" + " " +
                        "(source_table_name, successful_stops_percentage, missed_presses_percentage, " +
                        "incorrect_presses_percentage, correct_presses_percentage, average_latency_for_correct_presses, " +
                        "individual_time_dispersion) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, tableNameSource);
            statement.setString(2, String.format("%.2f%%", successfulStopsPercentage));
            statement.setString(3, String.format("%.2f%%", missedPressesPercentage));
            statement.setString(4, String.format("%.2f%%", incorrectPressesPercentage));
            statement.setString(5, String.format("%.2f%%", correctPressesPercentage));
            statement.setString(6, String.format("%.5f", averageLatencyForCorrectPresses));
            statement.setString(7, String.format("%.5f", individualTimeDispersion));
            statement.executeUpdate();
        }
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

}