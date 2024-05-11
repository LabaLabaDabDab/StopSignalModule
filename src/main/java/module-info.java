module nsu.fit.khomchenko.stopsignalmodule {
    requires java.sql;
    requires lombok;
    requires java.prefs;
    requires commons.math3;
    requires org.postgresql.jdbc;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.desktop;
    requires javafx.swing;


    opens nsu.fit.khomchenko.stopsignalmodule to javafx.fxml;
    exports nsu.fit.khomchenko.stopsignalmodule;
    exports nsu.fit.khomchenko.stopsignalmodule.controllers;
    opens nsu.fit.khomchenko.stopsignalmodule.controllers to javafx.fxml;
}