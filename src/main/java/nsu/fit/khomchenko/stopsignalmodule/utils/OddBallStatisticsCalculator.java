package nsu.fit.khomchenko.stopsignalmodule.utils;

import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OddBallStatisticsCalculator {
    public static List<String> calculateStatistics(List<OddBallData> dataList, String tableName, DatabaseSchema schema, boolean saveToDatabase) {
        double incorrectPressesOffTargetTonePercentage = calculateIncorrectPressesPercentage(dataList);
        double correctPressesTargetTonePercentage = calculateCorrectPressesPercentage(dataList);
        double averageReactionTime = calculateAverageReactionTime(dataList);
        double timeDispersion = calculateIndividualTimeDispersion(dataList);


        List<String> statistics = new ArrayList<>();
        statistics.add("Статистика испытуемого: " + tableName + "  по методике: " + schema);
        statistics.add("Процент некорректных нажатий после нецелевого тона: " + incorrectPressesOffTargetTonePercentage + "%");
        statistics.add("процент корректных нажатий после целевого тона: " + correctPressesTargetTonePercentage  + "%");
        statistics.add("Среднее время правильной реакции: " + averageReactionTime);
        statistics.add("Среднее квадратичное отклонение по правильной реакции: " + timeDispersion);

        if (saveToDatabase) {
            List<Double> values = new ArrayList<>();
            values.add(incorrectPressesOffTargetTonePercentage);
            values.add(correctPressesTargetTonePercentage);
            values.add(averageReactionTime);
            values.add(timeDispersion);

            List<String> columnNames = Arrays.asList(
                    "incorrect_presses_off_target_tone_percentage",
                    "correct_presses_target_tone_percentage",
                    "average_reaction_time",
                    "individual_time_dispersion"
            );

            DatabaseHandler.saveStatisticsToSummaryTable(schema, tableName, columnNames, values);
        }

        return statistics;
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
        return Math.sqrt(sumOfSquaredDifferences / countCorrectResponse);
    }
}