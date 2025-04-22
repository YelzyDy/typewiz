module com.oop2.typewiz {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.oop2.typewiz to javafx.fxml;
    exports com.oop2.typewiz;
}