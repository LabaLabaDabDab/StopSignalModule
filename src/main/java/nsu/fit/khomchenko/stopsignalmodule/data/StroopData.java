package nsu.fit.khomchenko.stopsignalmodule.data;

import lombok.Data;

@Data
public class StroopData {
    private String date;
    private String time;
    private String subject;
    private String blockcode;
    private String trialcode;
    private String stimulusitem1;
    private String stimulusitem2;
    private String stimulusitem3;
    private String stimulusitem4;
    private String trialnum;
    private String latency;
    private String response;
    private String correct;
}
