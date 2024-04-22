package nsu.fit.khomchenko.stopsignalmodule.utils;

import lombok.Data;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;

import java.util.*;
import java.util.stream.Collectors;


public class HuntStatisticsCalculator {
    public static Map<String, Map<String, String>> calculateStatistics(List<HuntData> tableData, String tableName, DatabaseSchema schemaName, boolean saveToDatabase) {
        double successfulStopsPercentage = calculateSuccessfulStopsPercentage(tableData);
        double missedPresses = countMissedPresses(tableData);
        double incorrectPresses = countIncorrectPresses(tableData);
        double correctPressesPercentage = calculateCorrectPressesPercentage(tableData);
        double averageLatencyForCorrectPresses = calculateAverageLatencyForCorrectPresses(tableData);
        double individualTimeDispersion = calculateIndividualTimeDispersion(tableData);

        Map<String, Map<String, String>> statisticsMap = new HashMap<>();

        String[] parts = tableName.split("_");
        String gender = "";
        int age = 0;
        String testName = "";

        if (parts.length >= 3) {
            gender = parts[1];
            try {
                age = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            StringBuilder testNameBuilder = new StringBuilder();
            for (int i = 3; i < parts.length; i++) {
                testNameBuilder.append(parts[i]);
                if (i < parts.length - 1) {
                    testNameBuilder.append("_");
                }
            }
            testName = testNameBuilder.toString();
        } else {
            System.err.println("Некорректный формат имени таблицы.");
        }

        Map<String, String> participantInfo = new HashMap<>();
        participantInfo.put("gender", gender);
        participantInfo.put("age", String.valueOf(age));
        participantInfo.put("testName", testName);
        statisticsMap.put("participant_info", participantInfo);

        Map<String, String> successfulStopsData = new HashMap<>();
        successfulStopsData.put("comment", "Процент успешных торможений");
        successfulStopsData.put("value", successfulStopsPercentage + "%");
        statisticsMap.put("successful_stops_percentage", successfulStopsData);

        Map<String, String> missedPressesData = new HashMap<>();
        missedPressesData.put("comment", "Количество пропущенных нажатий");
        missedPressesData.put("value", missedPresses + "%");
        statisticsMap.put("missed_presses_count", missedPressesData);

        Map<String, String> incorrectPressesData = new HashMap<>();
        incorrectPressesData.put("comment", "Количество неправильных нажатий");
        incorrectPressesData.put("value", incorrectPresses + "%");
        statisticsMap.put("incorrect_presses_count", incorrectPressesData);

        Map<String, String> correctPressesPercentageData = new HashMap<>();
        correctPressesPercentageData.put("comment", "Процент правильных нажатий");
        correctPressesPercentageData.put("value", correctPressesPercentage + "%");
        statisticsMap.put("correct_presses_percentage", correctPressesPercentageData);

        Map<String, String> averageLatencyForCorrectPressesData = new HashMap<>();
        averageLatencyForCorrectPressesData.put("comment", "Среднее время для правильных нажатий");
        averageLatencyForCorrectPressesData.put("value", String.valueOf(averageLatencyForCorrectPresses));
        statisticsMap.put("average_latency_for_correct_presses", averageLatencyForCorrectPressesData);

        Map<String, String> individualTimeDispersionData = new HashMap<>();
        individualTimeDispersionData.put("comment", "Среднее квадратичное отклонение по времени для правильных нажатий");
        individualTimeDispersionData.put("value", String.valueOf(individualTimeDispersion));
        statisticsMap.put("individual_time_dispersion", individualTimeDispersionData);

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
                .toList();

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
                .toList();

        double totalLatency = correctPresses.stream()
                .mapToDouble(data -> Double.parseDouble(data.getLatency()))
                .sum();

        return totalLatency / correctPresses.size();
    }

    //среднее квадратичное отклонение по времени(только для правильных)
    private static double calculateIndividualTimeDispersion(List<HuntData> dataList) {
        List<HuntData> correctPresses = dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) < 750 && Integer.parseInt(data.getCorrect()) == 1)
                .toList();

        double averageLatency = calculateAverageLatencyForCorrectPresses(dataList);

        double sumOfSquaredDifferences = correctPresses.stream()
                .mapToDouble(data -> Math.pow(Double.parseDouble(data.getLatency()) - averageLatency, 2))
                .sum();

        return Math.sqrt(sumOfSquaredDifferences / correctPresses.size() - 1);
    }
}