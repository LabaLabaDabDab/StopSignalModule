package nsu.fit.khomchenko.stopsignalmodule.utils;

import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;

import java.util.List;
import java.util.stream.Collectors;

public class OddBallStatisticsCalculator {

    public static String calculateStatistics(List<OddBallData> dataList, String tableName, DatabaseSchema schemaName) {
       /* if ("complex".equals(schemaName)) {
            return calculateHardOddBallStatistics(dataList, tableName, schemaName);
        } else if ("simple".equals(schemaName)) {
            return calculateSimpleOddBallStatistics(dataList, tableName, schemaName);
        } else {
            return "Unsupported schema: " + schemaName;
        }*/
        return tableName;
    }

    private static String calculateHardOddBallStatistics(List<OddBallData> dataList, String tableName, String schemaName) {
        return "Complex schema statistics";
    }

    private static String calculateSimpleOddBallStatistics(List<OddBallData> dataList, String tableName, String schemaName) {
        return "Simple schema statistics";
    }
}