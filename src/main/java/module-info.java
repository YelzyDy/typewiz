open module TypeWiz {
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media;

    // Ensure explicit visibility for FXGL entity classes
    requires com.almasb.fxgl.core;
    requires com.almasb.fxgl.entity;
    requires annotations;

    // Export game packages explicitly to FXGL
    exports com.oop2.typewiz;
    exports com.oop2.typewiz.GameplayComponents;
}