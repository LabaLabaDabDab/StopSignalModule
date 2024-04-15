package nsu.fit.khomchenko.stopsignalmodule.utils;

//import nsu.fit.khomchenko.stopsignalmodule.StopSignalData;

public class StopSignalStatisticsSummary {
    private double successfulStopsPercentage;
    private int missedPresses;
    private int incorrectPresses;
    private double correctPressesPercentage;
    private double averageLatencyForCorrectPresses;
    private double individualTimeDispersion;

    public StopSignalStatisticsSummary(double successfulStopsPercentage, int missedPresses, int incorrectPresses,
                                       double correctPressesPercentage, double averageLatencyForCorrectPresses,
                                       double individualTimeDispersion) {
        this.successfulStopsPercentage = successfulStopsPercentage;
        this.missedPresses = missedPresses;
        this.incorrectPresses = incorrectPresses;
        this.correctPressesPercentage = correctPressesPercentage;
        this.averageLatencyForCorrectPresses = averageLatencyForCorrectPresses;
        this.individualTimeDispersion = individualTimeDispersion;
    }

    // Геттеры
}

