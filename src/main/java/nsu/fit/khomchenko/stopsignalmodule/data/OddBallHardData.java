package nsu.fit.khomchenko.stopsignalmodule.data;

import lombok.Data;

@Data
public class OddBallHardData {
    private String date;
    private String time;
    private String subject;
    private String trialcode;
    private String trialnum;
    private String response;
    private String latency;
    private String correct;
    private String values_lasttrial;
    private String values_skipcount;
}
