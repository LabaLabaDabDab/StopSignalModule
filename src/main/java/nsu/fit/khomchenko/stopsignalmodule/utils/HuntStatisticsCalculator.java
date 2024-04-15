package nsu.fit.khomchenko.stopsignalmodule.utils;

import lombok.Data;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;

import java.util.List;
import java.util.stream.Collectors;


public class HuntStatisticsCalculator {
    public static String calculateStatistics(List<HuntData> tableData, String tableName, String schemaName) {
        double successfulStopsPercentage = calculateSuccessfulStopsPercentage(tableData);
        double missedPresses = countMissedPresses(tableData);
        double incorrectPresses = countIncorrectPresses(tableData);
        double correctPressesPercentage = calculateCorrectPressesPercentage(tableData);
        double averageLatencyForCorrectPresses = calculateAverageLatencyForCorrectPresses(tableData);
        double individualTimeDispersion = calculateIndividualTimeDispersion(tableData);


        StringBuilder statistics = new StringBuilder();
        statistics.append("Successful Stops Percentage: ").append(successfulStopsPercentage).append("%\n");
        statistics.append("Missed Presses: ").append(missedPresses).append("\n");
        statistics.append("Incorrect Presses: ").append(incorrectPresses).append("\n");
        statistics.append("Correct Presses Percentage: ").append(correctPressesPercentage).append("%\n");
        statistics.append("Average Latency for Correct Presses: ").append(averageLatencyForCorrectPresses).append("\n");
        statistics.append("Individual Time Dispersion: ").append(individualTimeDispersion).append("\n");


        DatabaseHandler.saveStatisticsToSummaryTable(schemaName, tableName,
                successfulStopsPercentage, missedPresses, incorrectPresses,
                correctPressesPercentage, averageLatencyForCorrectPresses,
                individualTimeDispersion);

        return statistics.toString();
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

    //Процент пропущенных нажатия
    private static double countMissedPresses(List<HuntData> dataList) {
        long correctPressesCount = dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) == 750)
                .count();
        return (double) correctPressesCount / dataList.size() * 100;
    }

    //Процент неправильных нажатия
    private static double countIncorrectPresses(List<HuntData> dataList) {
        long correctPressesCount = dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) < 750 && Integer.parseInt(data.getCorrect()) == 0)
                .count();
        return (double) correctPressesCount / dataList.size() * 100;
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

    //Индивидуальная дисперсия по времени(только для правильных) (среднее квадратичное отклонение по времени)
    private static double calculateIndividualTimeDispersion(List<HuntData> dataList) {
        List<HuntData> correctPresses = dataList.stream()
                .filter(data -> Integer.parseInt(data.getLatency()) < 750 && Integer.parseInt(data.getCorrect()) == 1)
                .toList();

        double averageLatency = calculateAverageLatencyForCorrectPresses(dataList);

        double sumOfSquaredDifferences = correctPresses.stream()
                .mapToDouble(data -> Math.pow(Double.parseDouble(data.getLatency()) - averageLatency, 2))
                .sum();

        return Math.sqrt(sumOfSquaredDifferences / correctPresses.size());
    }
}