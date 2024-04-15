package nsu.fit.khomchenko.stopsignalmodule.data;

import lombok.Data;

@Data
public class HuntData {
    private String date;
    private String time;
    private String subject;
    private String blockcode;
    private String trialcode;
    private String picture_target_currentitem;
    private String values_stiminterval;
    private String latency;
    private String response;
    private String correct;
    private String values_score;
    private String values_averagertpractice;
    private String values_ssdcoeffitient;
    private String values_ssd;
}
