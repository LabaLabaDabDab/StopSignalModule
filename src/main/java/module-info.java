module nsu.fit.khomchenko.stopsignalmodule {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens nsu.fit.khomchenko.stopsignalmodule to javafx.fxml;
    exports nsu.fit.khomchenko.stopsignalmodule;
}