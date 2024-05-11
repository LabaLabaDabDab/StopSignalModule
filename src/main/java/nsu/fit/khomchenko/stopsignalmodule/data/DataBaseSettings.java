package nsu.fit.khomchenko.stopsignalmodule.data;

import lombok.Data;

@Data
public class DataBaseSettings {
    public static String host;
    public static String port;
    public static String databaseName;
    public static String username;
    public static String password;
    public static String jdbcUrl;

    public static void setJdbcUrl(String jdbcUrl) {
        DataBaseSettings.jdbcUrl = jdbcUrl;
    }

    public static void setUsername(String username) {
        DataBaseSettings.username = username;
    }

    public static void setPassword(String password) {
        DataBaseSettings.password = password;
    }

    public static void setHost(String host) {
        DataBaseSettings.host = host;
    }

    public static void setPort(String port) {
        DataBaseSettings.port = port;
    }

    public static void setDatabaseName(String databaseName) {
        DataBaseSettings.databaseName = databaseName;
    }
}
