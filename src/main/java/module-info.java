module nsu.fit.khomchenko.stopsignalmodule {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires lombok;
    requires java.prefs;


    opens nsu.fit.khomchenko.stopsignalmodule to javafx.fxml;
    exports nsu.fit.khomchenko.stopsignalmodule;
    exports nsu.fit.khomchenko.stopsignalmodule.controllers;
    opens nsu.fit.khomchenko.stopsignalmodule.controllers to javafx.fxml;
}