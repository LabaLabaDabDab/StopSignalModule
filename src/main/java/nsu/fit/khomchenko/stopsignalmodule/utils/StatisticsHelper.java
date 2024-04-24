package nsu.fit.khomchenko.stopsignalmodule.utils;

import java.util.HashMap;
import java.util.Map;

public class StatisticsHelper {
    public static Map<String, String> extractParticipantInfo(String tableName) {
        Map<String, String> participantInfo = new HashMap<>();
        String[] parts = tableName.split("_");

        if (parts.length >= 4) {
            String gender = parts[1];
            int age = 0;

            try {
                age = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            StringBuilder testNameBuilder = new StringBuilder();
            for (int i = 3; i < parts.length; i++) {
                testNameBuilder.append(parts[i]);
                if (i < parts.length - 1) {
                    testNameBuilder.append("_");
                }
            }
            String testName = testNameBuilder.toString();

            participantInfo.put("gender", gender);
            participantInfo.put("age", String.valueOf(age));
            participantInfo.put("testName", testName);
        } else {
            System.err.println("Некорректный формат имени таблицы.");
        }

        return participantInfo;
    }
}