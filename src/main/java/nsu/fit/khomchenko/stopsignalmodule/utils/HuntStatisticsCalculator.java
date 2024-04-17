package nsu.fit.khomchenko.stopsignalmodule.utils;

import lombok.Data;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseHandler;
import nsu.fit.khomchenko.stopsignalmodule.DatabaseSchema;
import nsu.fit.khomchenko.stopsignalmodule.data.HuntData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class HuntStatisticsCalculator {
    public static List<String> calculateStatistics(List<HuntData> tableData, String tableName, DatabaseSchema schemaName, boolean saveToDatabase) {
        double successfulStopsPercentage = calculateSuccessfulStopsPercentage(tableData);
        double missedPresses = countMissedPresses(tableData);
        double incorrectPresses = countIncorrectPresses(tableData);
        double correctPressesPercentage = calculateCorrectPressesPercentage(tableData);
        double averageLatencyForCorrectPresses = calculateAverageLatencyForCorrectPresses(tableData);
        double individualTimeDispersion = calculateIndividualTimeDispersion(tableData);

        List<String> statistics = new ArrayList<>();
        statistics.add("Статистика испытуемого: " + tableName + " по методике: " + schemaName.getDisplayName());
        statistics.add("Процент успешных торможений: " + successfulStopsPercentage + "%");
        statistics.add("Процент пропущенных нажатий: " + missedPresses + "%");
        statistics.add("Процент неправильных нажатий: " + incorrectPresses + "%");
        statistics.add("Процент правильных нажатий: " + correctPressesPercentage + "%");
        statistics.add("Среднее время для правильных нажатий: " + averageLatencyForCorrectPresses);
        statistics.add("Индивидуальная дисперсия по времени (только для правильных нажатий): " + individualTimeDispersion);

        if (saveToDatabase) {
            List<Double> values = new ArrayList<>();
            values.add(successfulStopsPercentage);
            values.add(missedPresses);
            values.add(incorrectPresses);
            values.add(correctPressesPercentage);
            values.add(averageLatencyForCorrectPresses);
            values.add(individualTimeDispersion);

            List<String> columnNames = Arrays.asList(
                    "successful_stops_percentage",
                    "missed_presses_percentage",
                    "incorrect_presses_percentage",
                    "correct_presses_percentage",
                    "average_reaction_time",
                    "individual_time_dispersion"
            );

            DatabaseHandler.saveStatisticsToSummaryTable(schemaName, tableName, columnNames, values);
        }

        return statistics;
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