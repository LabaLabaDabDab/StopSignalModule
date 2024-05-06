package nsu.fit.khomchenko.stopsignalmodule.utils;

import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.StroopData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nsu.fit.khomchenko.stopsignalmodule.utils.StatisticsHelper.createMap;

public class StroopStatisticsCalculator {
    public static Map<String, Map<String, String>> calculateStatistics(List<StroopData> tableData, String tableName, DatabaseSchema schemaName, boolean saveToDatabase) {
        Map<String, Map<String, String>> statisticsMap = new HashMap<>();
        Map<String, String> participantInfo = StatisticsHelper.extractParticipantInfo(tableName);
        statisticsMap.put("participant_info", participantInfo);

        List<StroopData> filteredData = tableData.stream()
                .filter(data -> !data.getTrialcode().equals("Warning") &&
                        !data.getTrialcode().equals("Black") &&
                        !data.getTrialcode().equals("White"))
                .collect(Collectors.toList());

        double successfulAnswersAllPercentage = calculateSuccessfulAnswersAllPercentage(filteredData);
        double averageLatencyForCorrectAllPresses = calculateAverageLatencyForCorrectPresses(filteredData);
        double individualTimeAllDispersion = calculateIndividualTimeDispersion(filteredData);
        double successfulAnswersStroopPercentage = calculateSuccessfulAnswersStroopPercentage(filteredData);
        double averageLatencyForCorrectStroopPresses = calculateAverageLatencyForCorrectStroopPresses(filteredData);
        double individualTimeStroopDispersion = calculateIndividualTimeStroopDispersion(filteredData);
        double successfulAnswersNonStroopPercentage = calculateSuccessfulAnswersNonStroopPercentage(filteredData);
        double averageLatencyForCorrectNonStroopPresses = calculateAverageLatencyForCorrectNonStroopPresses(filteredData);
        double individualTimeNonStroopDispersion = calculateIndividualTimeNonStroopDispersion(filteredData);

        statisticsMap.put("successful_answers_all_percentage", createMap("Процент правильных ответов (все)", successfulAnswersAllPercentage));
        statisticsMap.put("average_latency_for_correct_all_presses", createMap("Среднее время правильной реакции (все)", averageLatencyForCorrectAllPresses));
        statisticsMap.put("individual_time_all_dispersion", createMap("Стандартное отклонение времени правильной реакции (все)", individualTimeAllDispersion));
        statisticsMap.put("successful_answers_stroop_percentage", createMap("Процент правильных ответов (струп)", successfulAnswersStroopPercentage));
        statisticsMap.put("average_latency_for_correct_stroop_presses", createMap("Среднее время правильной реакции (струп)", averageLatencyForCorrectStroopPresses));
        statisticsMap.put("individual_time_stroop_dispersion", createMap("Стандартное отклонение времени правильной реакции (струп)", individualTimeStroopDispersion));
        statisticsMap.put("successful_answers_non_stroop_percentage", createMap("Процент правильных ответов (не-струп)", successfulAnswersNonStroopPercentage));
        statisticsMap.put("average_latency_for_correct_non_stroop_presses", createMap("Среднее время правильной реакции (не-струп)", averageLatencyForCorrectNonStroopPresses));
        statisticsMap.put("individual_time_non_stroop_dispersion", createMap("Стандартное отклонение времени правильной реакции (не-струп)", individualTimeNonStroopDispersion));

        if (saveToDatabase) {
            List<Double> values = Arrays.asList(
                    successfulAnswersAllPercentage,
                    averageLatencyForCorrectAllPresses,
                    individualTimeAllDispersion,
                    successfulAnswersStroopPercentage,
                    averageLatencyForCorrectStroopPresses,
                    individualTimeStroopDispersion,
                    successfulAnswersNonStroopPercentage,
                    averageLatencyForCorrectNonStroopPresses,
                    individualTimeNonStroopDispersion
            );

            List<String> columnNames = Arrays.asList(
                    "successful_answers_all_percentage",
                    "average_latency_for_correct_all_presses",
                    "individual_time_all_dispersion",
                    "successful_answers_stroop_percentage",
                    "average_latency_for_correct_stroop_presses",
                    "individual_time_stroop_dispersion",
                    "successful_answers_non_stroop_percentage",
                    "average_latency_for_correct_non_stroop_presses",
                    "individual_time_non_stroop_dispersion"
            );

            DatabaseHandler.saveStatisticsToSummaryTable(schemaName, tableName, columnNames, values);
        }

        return statisticsMap;
    }


    //Процент правильных ответов (все)
    private static double calculateSuccessfulAnswersAllPercentage(List<StroopData> tableData) {
        int totalAnswers = tableData.size();

        long correctAnswersCount = tableData.stream()
                .filter(data -> data.getCorrect().equals("1"))
                .count();

        return (double) correctAnswersCount / totalAnswers * 100.0;
    }

    //Процент правильных ответов (струп)
    private static double calculateSuccessfulAnswersStroopPercentage(List<StroopData> tableData) {
        long totalAnswers = tableData.stream()
                .filter(data -> data.getTrialcode().equals("BBLT") || data.getTrialcode().equals("LTBB"))
                .count();

        long correctAnswersCount = tableData.stream()
                .filter(data -> (data.getTrialcode().equals("BBLT") || data.getTrialcode().equals("LTBB") ) && data.getCorrect().equals("1"))
                .count();

        return (double) correctAnswersCount / totalAnswers * 100.0;
    }

    //Процент правильных ответов (не струп)
    private static double calculateSuccessfulAnswersNonStroopPercentage(List<StroopData> tableData) {
        long totalAnswers = tableData.stream()
                .filter(data -> data.getTrialcode().equals("BTLB") || data.getTrialcode().equals("LBBT"))
                .count();


        long correctAnswersCount = tableData.stream()
                .filter(data -> (data.getTrialcode().equals("BTLB") || data.getTrialcode().equals("LBBT") ) && data.getCorrect().equals("1"))
                .count();

        return (double) correctAnswersCount / totalAnswers * 100.0;
    }

    //Среднее время правильной реакции (все)
    private static double calculateAverageLatencyForCorrectStroopPresses(List<StroopData> dataList) {
        List<StroopData> filteredData = dataList.stream()
                .filter(data -> data.getCorrect().equals("1"))
                .collect(Collectors.toList());

        double totalReactionTime = filteredData.stream()
                .mapToDouble(data -> Double.parseDouble(data.getLatency() + 350))
                .sum();

        return totalReactionTime / dataList.size();
    }

    //среднее квадратичное отклонение по правильной реакции (все)
    private static double calculateIndividualTimeDispersion(List<StroopData> dataList) {
        double meanReactionTime = calculateAverageLatencyForCorrectPresses(dataList);

        int countCorrectResponse = (int) dataList.stream()
                .filter(data -> data.getCorrect().equals("1"))
                .count();

        double sumOfSquaredDifferences = dataList.stream()
                .filter(data -> data.getCorrect().equals("1"))
                .mapToDouble(data -> {
                    double latency = Double.parseDouble(data.getLatency()) + 350;
                    return Math.pow(latency - meanReactionTime, 2);
                })
                .sum();

        return Math.sqrt(sumOfSquaredDifferences / (countCorrectResponse - 1));
    }

    //Среднее время правильной реакции (струп)
    private static double calculateAverageLatencyForCorrectPresses(List<StroopData> dataList) {
        List<StroopData> filteredData = dataList.stream()
                .filter(data -> (data.getTrialcode().equals("BBLT") || data.getTrialcode().equals("LTBB")) && data.getCorrect().equals("1"))
                .collect(Collectors.toList());

        double totalReactionTime = filteredData.stream()
                .mapToDouble(data -> Double.parseDouble(data.getLatency() + 350))
                .sum();

        return totalReactionTime / dataList.size();
    }

    //среднее квадратичное отклонение по правильной реакции (струп)
    private static double calculateIndividualTimeStroopDispersion(List<StroopData> dataList) {
        double meanReactionTime = calculateAverageLatencyForCorrectPresses(dataList);

        int countCorrectResponse = (int) dataList.stream()
                .filter(data -> (data.getTrialcode().equals("BBLT") || data.getTrialcode().equals("LTBB") ) && data.getCorrect().equals("1"))
                .count();

        double sumOfSquaredDifferences = dataList.stream()
                .filter(data -> (data.getTrialcode().equals("BBLT") || data.getTrialcode().equals("LTBB") ) && data.getCorrect().equals("1"))
                .mapToDouble(data -> {
                    double latency = Double.parseDouble(data.getLatency()) + 350;
                    return Math.pow(latency - meanReactionTime, 2);
                })
                .sum();

        return Math.sqrt(sumOfSquaredDifferences / (countCorrectResponse - 1));
    }

    //Среднее время правильной реакции (не струп)
    private static double calculateAverageLatencyForCorrectNonStroopPresses(List<StroopData> dataList) {
        List<StroopData> filteredData = dataList.stream()
                .filter(data -> (data.getTrialcode().equals("BTLB") || data.getTrialcode().equals("LBBT") ) && data.getCorrect().equals("1"))
                .collect(Collectors.toList());

        double totalReactionTime = filteredData.stream()
                .mapToDouble(data -> Double.parseDouble(data.getLatency() + 350))
                .sum();

        return totalReactionTime / dataList.size();
    }

    //среднее квадратичное отклонение по правильной реакции (не струп)
    private static double calculateIndividualTimeNonStroopDispersion(List<StroopData> dataList) {
        double meanReactionTime = calculateAverageLatencyForCorrectPresses(dataList);

        int countCorrectResponse = (int) dataList.stream()
                .filter(data -> (data.getTrialcode().equals("BTLB") || data.getTrialcode().equals("LBBT") ) && data.getCorrect().equals("1"))
                .count();

        double sumOfSquaredDifferences = dataList.stream()
                .filter(data -> (data.getTrialcode().equals("BTLB") || data.getTrialcode().equals("LBBT") ) && data.getCorrect().equals("1"))
                .mapToDouble(data -> {
                    double latency = Double.parseDouble(data.getLatency()) + 350;
                    return Math.pow(latency - meanReactionTime, 2);
                })
                .sum();

        return Math.sqrt(sumOfSquaredDifferences / (countCorrectResponse - 1));
    }
}