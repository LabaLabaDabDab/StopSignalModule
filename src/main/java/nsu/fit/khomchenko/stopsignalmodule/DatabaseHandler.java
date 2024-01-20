package nsu.fit.khomchenko.stopsignalmodule;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static void loadAndSaveData(String filePath, String tableName) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                createTable(tableName, headerLine);

                String line;
                while ((line = br.readLine()) != null) {
                    saveDataRow(tableName, line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createTable(String tableName, String headerLine) {
        try (Connection connection = connect()) {
            assert connection != null;
            try (Statement statement = connection.createStatement()) {
                String[] columns = headerLine.replace(".", "_").split("\t");

                StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                createTableQuery.append(tableName).append(" (");
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


    private static void saveDataRow(String tableName, String dataRow) {
        try (Connection connection = connect()) {
            if (connection != null) {
                try {
                    String[] values = dataRow.split("\t");

                    StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
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

    public static List<String> getAllTables() {
        List<String> tableNames = new ArrayList<>();

        try (Connection connection = connect()) {
            if (connection != null) {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});

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

    public static List<String[]> getDataForTable(String tableName) {
        List<String[]> tableData = new ArrayList<>();

        try (Connection connection = connect()) {
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

    public static List<String> getColumnNames(String tableName) {
        List<String> columnNames = new ArrayList<>();

        try (Connection connection = connect()) {
            if (connection != null) {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet resultSet = metaData.getColumns(null, null, tableName, null);

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

    public static boolean deleteTable(String tableName) {
        try (Connection connection = connect()) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean saveTableAs(String tableName, File file, String format) {
        List<String[]> tableData = getDataForTable(tableName);
        List<String> columnNames = getColumnNames(tableName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            switch (format.toLowerCase()) {
                case "txt":
                    saveAsTxt(tableData, columnNames, writer);
                    break;
                case "csv":
                    saveAsCsv(tableData, columnNames, writer);
                    break;
                case "iqdat":
                    saveAsIqdat(tableData, columnNames, writer);
                    break;
                default:
                    return false; // Неподдерживаемый формат
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
        writer.write(String.join(",", columnNames));
        writer.newLine();

        for (String[] row : tableData) {
            writer.write(String.join(",", row));
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
}