package nsu.fit.khomchenko.stopsignalmodule.utils;

import lombok.Data;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;

import java.util.*;
import java.util.stream.Collectors;

import static nsu.fit.khomchenko.stopsignalmodule.utils.StatisticsHelper.createMap;


public class HuntStatisticsCalculator {
    public static Map<String, Map<String, String>> calculateStatistics(List<HuntData> tableData, String tableName, DatabaseSchema schemaName, boolean saveToDatabase) {
        double successfulStopsPercentage = calculateSuccessfulStopsPercentage(tableData);
        double missedPresses = countMissedPresses(tableData);
        double incorrectPresses = countIncorrectPresses(tableData);
        double correctPressesPercentage = calculateCorrectPressesPercentage(tableData);
        double averageLatencyForCorrectPresses = calculateAverageLatencyForCorrectPresses(tableData);
        double individualTimeDispersion = calculateIndividualTimeDispersion(tableData);

        Map<String, Map<String, String>> statisticsMap = new HashMap<>();
        Map<String, String> participantInfo = StatisticsHelper.extractParticipantInfo(tableName);
        statisticsMap.put("participant_info", participantInfo);


        statisticsMap.put("successful_stops_percentage", createMap("Процент успешных торможений", successfulStopsPercentage));
        statisticsMap.put("missed_presses_count", createMap("Количество пропущенных нажатий", missedPresses));
        statisticsMap.put("incorrect_presses_count", createMap("Количество неправильных нажатий", incorrectPresses));
        statisticsMap.put("correct_presses_percentage", createMap("Процент правильных нажатий", correctPressesPercentage));
        statisticsMap.put("average_latency_for_correct_presses", createMap("Среднее время для правильных нажатий", averageLatencyForCorrectPresses));
        statisticsMap.put("individual_time_dispersion", createMap("Стандартное отклонение по времени для правильных нажатий", individualTimeDispersion));


        if (saveToDatabase) {
            List<Double> values = Arrays.asList(
                    successfulStopsPercentage,
                    missedPresses,
                    incorrectPresses,
                    correctPressesPercentage,
                    averageLatencyForCorrectPresses,
                    individualTimeDispersion
            );

            List<String> columnNames = Arrays.asList(
                    "successful_stops_percentage",
                    "missed_presses_count",
                    "incorrect_presses_count",
                    "correct_presses_percentage",
                    "average_latency_for_correct_presses",
                    "individual_time_dispersion"
            );

            DatabaseHandler.saveStatisticsToSummaryTable(schemaName, tableName, columnNames, values);
        }

        return statisticsMap;
    }


    //Процент успешных торможений
    private static double calculateSuccessfulStopsPercentage(List<HuntData> dataList) {
        List<HuntData> filteredData = dataList.stream()
                .filter(data -> "CRTTstop2".equals(data.getTrialcode()))
                .collect(Collectors.toList());

        // Подсчитываем количество успешных остановок среди отфильтрованных данных
        long successfulStopsCount = filteredData.stream()
                .filter(data -> Integer.parseInt(data.getResponse()) == 0)
                .count();

        // Вычисляем процент успешных остановок
        return (double) successfulStopsCount / filteredData.size() * 100;
    }

    //Количество пропущенных нажатий
    private static double countMissedPresses(List<HuntData> dataList) {
        return dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) == 750)
                .count();
    }

    //Количество неправильных нажатий
    private static double countIncorrectPresses(List<HuntData> dataList) {
        return dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) < 750 && Integer.parseInt(data.getCorrect()) == 0)
                .count();
    }


    //Процент правильных нажатий
    private static double calculateCorrectPressesPercentage(List<HuntData> dataList) {
        long correctPressesCount = dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) < 750 && Integer.parseInt(data.getCorrect()) == 1)
                .count();

        return (double) correctPressesCount / dataList.size() * 100;
    }

    //Среднее время для правильных нажатий:
    private static double calculateAverageLatencyForCorrectPresses(List<HuntData> dataList) {
        List<HuntData> correctPresses = dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) < 750 && Integer.parseInt(data.getCorrect()) == 1)
                .collect(Collectors.toList());

        double totalLatency = correctPresses.stream()
                .mapToDouble(data -> Double.parseDouble(data.getLatency()))
                .sum();

        return totalLatency / correctPresses.size();
    }

    //среднее квадратичное отклонение по времени(только для правильных)
    private static double calculateIndividualTimeDispersion(List<HuntData> dataList) {
        List<HuntData> correctPresses = dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) < 750 && Integer.parseInt(data.getCorrect()) == 1)
                .collect(Collectors.toList());

        double averageLatency = calculateAverageLatencyForCorrectPresses(dataList);

        double sumOfSquaredDifferences = correctPresses.stream()
                .mapToDouble(data -> Math.pow(Double.parseDouble(data.getLatency()) - averageLatency, 2))
                .sum();

        return Math.sqrt(sumOfSquaredDifferences / correctPresses.size() - 1);
    }
}