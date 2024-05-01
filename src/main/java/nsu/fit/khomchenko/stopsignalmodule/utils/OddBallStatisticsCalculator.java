package nsu.fit.khomchenko.stopsignalmodule.utils;

import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;

import java.util.*;

import static nsu.fit.khomchenko.stopsignalmodule.utils.StatisticsHelper.createMap;

public class OddBallStatisticsCalculator {
    public static Map<String, Map<String, String>> calculateStatistics(List<OddBallData> dataList, String tableName, DatabaseSchema schema, boolean saveToDatabase) {
        double incorrectPressesOffTargetTonePercentage = calculateIncorrectPressesPercentage(dataList);
        double correctPressesTargetTonePercentage = calculateCorrectPressesPercentage(dataList);
        double averageReactionTime = calculateAverageReactionTime(dataList);
        double timeDispersion = calculateIndividualTimeDispersion(dataList);
        double prematurePresses = calculatePrematurePresses(dataList);

        Map<String, Map<String, String>> statisticsMap = new HashMap<>();

        Map<String, String> participantInfo = StatisticsHelper.extractParticipantInfo(tableName);
        statisticsMap.put("participant_info", participantInfo);

        statisticsMap.put("incorrect_presses_off_target_tone_percentage", createMap("Процент некорректных нажатий после нецелевого тона", incorrectPressesOffTargetTonePercentage));
        statisticsMap.put("correct_presses_target_tone_percentage", createMap("Процент корректных нажатий после целевого тона", correctPressesTargetTonePercentage));
        statisticsMap.put("average_reaction_time", createMap("Среднее время правильной реакции", averageReactionTime));
        statisticsMap.put("individual_time_dispersion", createMap("Среднее квадратичное отклонение по правильной реакции", timeDispersion));
        statisticsMap.put("premature_presses", createMap("Среднее количество преждевременных нажатий", prematurePresses));

        if (saveToDatabase) {
            List<Double> values = Arrays.asList(
                    incorrectPressesOffTargetTonePercentage,
                    correctPressesTargetTonePercentage,
                    averageReactionTime,
                    timeDispersion,
                    prematurePresses
            );

            List<String> columnNames = Arrays.asList(
                    "incorrect_presses_off_target_tone_percentage",
                    "correct_presses_target_tone_percentage",
                    "average_reaction_time",
                    "individual_time_dispersion",
                    "premature_presses"
            );

            DatabaseHandler.saveStatisticsToSummaryTable(schema, tableName, columnNames, values);
        }

        return statisticsMap;
    }

    //процент некорректных нажатий после нецелевого тона
    private static double calculateIncorrectPressesPercentage(List<OddBallData> dataList) {
        long totalResponses = dataList.stream()
                .filter(data -> data.getTrialcode().equals("baseline"))
                .count();

        long incorrectResponses = dataList.stream()
                .filter(data -> data.getTrialcode().equals("baseline") && !data.getResponse().equals("0"))
                .count();

        return (double) (incorrectResponses * 100) / totalResponses;
    }

    //процент корректных нажатий после целевого тона
    private static double calculateCorrectPressesPercentage(List<OddBallData> dataList) {
        long totalResponses = dataList.stream()
                .filter(data -> data.getTrialcode().equals("oddball"))
                .count();

        long incorrectResponses = dataList.stream()
                .filter(data -> data.getTrialcode().equals("oddball") && !data.getResponse().equals("0"))
                .count();

        return (double) (incorrectResponses * 100) / totalResponses;
    }

    //Преждевременные нажатия
    private static double calculatePrematurePresses(List<OddBallData> dataList) {
        long totalResponses = dataList.stream()
                .filter(data -> {
                    try {
                        int latency = Integer.parseInt(data.getLatency());
                        return latency < 250;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .count();

        if (dataList.isEmpty()) {
            return 0.0;
        }

        return (double) totalResponses / dataList.size();
    }

    //среднее время правильной реакции
    private static double calculateAverageReactionTime(List<OddBallData> dataList) {
        List<OddBallData> filteredData = dataList.stream()
                .filter(data -> (data.getTrialcode().equals("oddball") && !data.getResponse().equals("0")) ||
                        (data.getTrialcode().equals("baseline") && data.getResponse().equals("0")))
                .toList();

        double totalReactionTime = filteredData.stream()
                .mapToDouble(data -> Double.parseDouble(data.getLatency()))
                .sum();

        return totalReactionTime / filteredData.size();
    }

    //среднее квадратичное отклонение по правильной реакции
    private static double calculateIndividualTimeDispersion(List<OddBallData> dataList) {
        double countCorrectResponse = dataList.stream()
                .filter(data -> (data.getTrialcode().equals("oddball") && !data.getResponse().equals("0")) ||
                        (data.getTrialcode().equals("baseline") && data.getResponse().equals("0")))
                .count();

        double meanReactionTime = calculateAverageReactionTime(dataList);

        double sumOfSquaredDifferences = dataList.stream()
                .filter(data -> (data.getTrialcode().equals("oddball") && !data.getResponse().equals("0")) ||
                        (data.getTrialcode().equals("baseline") && data.getResponse().equals("0")))
                .mapToDouble(data -> Math.pow(Double.parseDouble(data.getLatency()) - meanReactionTime, 2))
                .sum();
        return Math.sqrt(sumOfSquaredDifferences / countCorrectResponse - 1);
    }
}