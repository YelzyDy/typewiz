open module TypeWiz {
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    // Add explicit requires for entity module
    requires transitive javafx.base;
    requires transitive javafx.media;
    
    // Export game packages explicitly to FXGL
    exports com.oop2.typewiz.GameplayComponents to com.almasb.fxgl.all;
}