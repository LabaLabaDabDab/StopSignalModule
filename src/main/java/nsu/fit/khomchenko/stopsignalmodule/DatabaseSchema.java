package nsu.fit.khomchenko.stopsignalmodule;

public enum DatabaseSchema {
    STOP_SIGNAL("stop_signal", "Stop Signal");

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
