package nsu.fit.khomchenko.stopsignalmodule.utils;

import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.OddBallData;

import java.util.List;

public class OddBallStatisticsCalculator {
    public static String calculateStatistics(List<OddBallData> dataList, String tableName, DatabaseSchema schema, boolean saveToDatabase) {
        double incorrectPressesPercentage = calculateIncorrectPressesPercentage(dataList);
        double correctPressesPercentage = calculateCorrectPressesPercentage(dataList);
        double averageReactionTime = calculateAverageReactionTime(dataList);
        double timeDispersion = calculateIndividualTimeDispersion(dataList);

        StringBuilder statistics = new StringBuilder();
        statistics.append("Statistics for table: ").append(tableName).append(" in schema: ").append(schema.getDisplayName()).append("\n");
        statistics.append("Incorrect Presses Percentage: ").append(incorrectPressesPercentage).append("%\n");
        statistics.append("Correct Presses Percentage: ").append(correctPressesPercentage).append("%\n");
        statistics.append("Average Reaction Time: ").append(averageReactionTime).append("\n");
        statistics.append("Time Dispersion: ").append(timeDispersion).append("\n");

        if (saveToDatabase) {
            DatabaseHandler.saveStatisticsToSummaryTable(schema, tableName,
                    0.0, 0.0, incorrectPressesPercentage, correctPressesPercentage,
                    averageReactionTime, timeDispersion);
        }

        return statistics.toString();
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