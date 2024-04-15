package nsu.fit.khomchenko.stopsignalmodule;

public enum DatabaseSchema {
    HUNT("hunt", "Hunt"),
    ODD_BALL_EASY("odd_ball_easy", "Odd Ball Easy"),
    ODD_BALL_HARD("odd_ball_hard", "Odd Ball Hard"),
    STROOP("stroop", "Stroop");

    private final String schemaName;
    private final String displayName;

    DatabaseSchema(String schemaName, String displayName) {
        this.schemaName = schemaName;
        this.displayName = displayName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
