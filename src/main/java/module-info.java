module com.oop2.typewiz {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.oop2.typewiz to javafx.fxml;
    exports com.oop2.typewiz;
}