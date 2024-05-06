package nsu.fit.khomchenko.stopsignalmodule.utils;

import java.util.HashMap;
import java.util.Map;

public class StatisticsHelper {
    public static Map<String, String> createMap(String comment, double value) {
        Map<String, String> data = new HashMap<>();
        data.put("comment", comment);
        String valueString = String.valueOf(value);
        if (comment.startsWith("Процент")) {
            valueString += "%";
        }
        data.put("value", valueString);
        return data;
    }

    public static Map<String, String> extractParticipantInfo(String tableName) {
        Map<String, String> participantInfo = new HashMap<>();
        String[] parts = tableName.split("_");

        if (parts.length >= 3) {
            String testName = parts[0];

            String gender = parts[1];
            int age = 0;

            try {
                age = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            String testPoint = null;

            if (parts.length == 4){
                testPoint = parts[3];
            }

            participantInfo.put("gender", gender);
            participantInfo.put("age", String.valueOf(age));
            participantInfo.put("testName", testName);
            participantInfo.put("testPoint", testPoint);
        } else {
            System.err.println("Некорректный формат имени таблицы.");
        }

        return participantInfo;
    }
}