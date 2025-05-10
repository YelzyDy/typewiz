package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.util.ThreadManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class DifficultyMenuScreen extends FXGLMenu {
    public Pane getAsPane() {
        return (Pane) getContentRoot();
    }
    public static Node create(Runnable backAction, Runnable easyAction, Runnable mediumAction, Runnable hardAction) {
        return new DifficultyMenuScreen(backAction, easyAction, mediumAction, hardAction).getContentRoot();
    }

    DifficultyMenuScreen(Runnable backAction, Runnable easyAction, Runnable mediumAction, Runnable hardAction) {
        super(MenuType.GAME_MENU);

        // Main container with magical gradient
        StackPane root = new StackPane();
        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2a0845, #4a148c);");

        // Glass panel effect
        Rectangle panel = new Rectangle(500, 500);
        panel.setArcHeight(30);
        panel.setArcWidth(30);
        panel.setFill(Color.web("rgba(60, 0, 90, 0.5)"));
        panel.setStroke(Color.web("#b388ff"));
        panel.setStrokeWidth(2);
        panel.setEffect(new javafx.scene.effect.DropShadow(20, Color.web("#ffeb3b", 0.3)));

        // Title
        Text title = new Text("Choose Your Magic Level");
        title.setFont(Font.font("Papyrus", 36));
        title.setFill(Color.web("#ffeb3b"));
        title.setEffect(new javafx.scene.effect.Glow(0.5));

        // Difficulty buttons
        Button easyButton = createDifficultyButton("Apprentice", "Slow incantations", easyAction);
        Button mediumButton = createDifficultyButton("Wizard", "Standard spells", mediumAction);
        Button hardButton = createDifficultyButton("Archmage", "Lightning-fast charms", hardAction);

        // Back button
        Button backButton = new Button("Back to Tower");
        backButton.setFont(Font.font("Consolas", 20));
        backButton.setTextFill(Color.web("#e2b0ff"));
        backButton.setBackground(Background.EMPTY);
        backButton.setBorder(new Border(new BorderStroke(
                Color.web("#b388ff"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1))
        ));
        backButton.setOnAction(e -> backAction.run());

        // Layout
        VBox menuBox = new VBox(20, title, easyButton, mediumButton, hardButton, backButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);

        StackPane glassPane = new StackPane(panel, menuBox);
        root.getChildren().add(glassPane);
        getContentRoot().getChildren().add(root);
    }

    private Button createDifficultyButton(String title, String description, Runnable action) {
        Button button = new Button();
        button.setPrefWidth(350);
        button.setPrefHeight(100);

        // Main title
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Papyrus", 28));
        titleText.setFill(Color.web("#ffeb3b"));

        // Description
        Text descText = new Text(description);
        descText.setFont(Font.font("Consolas", 16));
        descText.setFill(Color.web("#d1c4e9"));

        VBox content = new VBox(5, titleText, descText);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new javafx.geometry.Insets(0, 20, 0, 20));

        button.setGraphic(content);

        // Styling
        button.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT,
                new CornerRadii(10),
                javafx.geometry.Insets.EMPTY
        )));

        button.setBorder(new Border(new BorderStroke(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#b388ff")),
                        new Stop(1, Color.web("#ffeb3b"))),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(3))
        ));


        // Hover effects (Scale Animation)
        button.setOnMouseEntered(e -> {
            ThreadManager.runAsyncThenUI(
                    () -> {
                        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), button);
                        scaleUp.setToX(1.1);
                        scaleUp.setToY(1.1);
                        scaleUp.play();
                    },
                    () -> {} // UI thread callback
            );
        });

        button.setOnMouseExited(e -> {
            ThreadManager.runAsyncThenUI(
                    () -> {
                        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), button);
                        scaleDown.setToX(1);
                        scaleDown.setToY(1);
                        scaleDown.play();
                    },
                    () -> {} // UI thread callback
            );
        });

        // Click effect (Scale Animation)
        button.setOnAction(e -> {
            ThreadManager.runAsyncThenUI(
                    () -> {
                        ScaleTransition scaleClick = new ScaleTransition(Duration.seconds(0.1), button);
                        scaleClick.setToX(0.9);
                        scaleClick.setToY(0.9);
                        scaleClick.setOnFinished(event -> action.run());
                        scaleClick.play();
                    },
                    () -> {} // UI thread callback
            );
        });


        return button;
    }
}



//package com.oop2.typewiz;
//
//import com.almasb.fxgl.dsl.FXGL;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.effect.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.*;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.scene.text.Text;
//
//public class DifficultyMenuScreen {
//
//
//    public static Pane create(Runnable onBack, Runnable onEasy, Runnable onMedium, Runnable onHard) {
//        StackPane root = new StackPane();
//        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
//
//        // Background
//        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
//        bg.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
//                new Stop(0, Color.web("#2a0845")),
//                new Stop(1, Color.web("#1a0638"))));
//        bg.setEffect(new Bloom(0.1));
//
//        // Title
//        Text title = new Text("Choose Your Path");
//        title.setFont(Font.font("Papyrus", FontWeight.BOLD, 48));
//        title.setFill(Color.web("#ffeb3b"));
//        title.setEffect(new DropShadow(15, Color.web("#ffeb3b", 0.7)));
//
//        VBox buttons = new VBox(20,
//                createButton("Apprentice (Easy)", onEasy),
//                createButton("Adept (Medium)", onMedium),
//                createButton("Archmage (Hard)", onHard));
//        buttons.setAlignment(Pos.CENTER);
//
//        Button backBtn = new Button("⬅ BACK");
//        backBtn.setFont(Font.font("Consolas", 18));
//        backBtn.setTextFill(Color.web("#e2b0ff"));
//        backBtn.setBackground(Background.EMPTY);
//        backBtn.setBorder(new Border(new BorderStroke(Color.web("#e2b0ff"),
//                BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(2))));
//        backBtn.setOnAction(e -> onBack.run());
//
//        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
//        StackPane.setMargin(backBtn, new javafx.geometry.Insets(20));
//
//        VBox vbox = new VBox(40, title, buttons);
//        vbox.setAlignment(Pos.CENTER);
//
//        root.getChildren().addAll(bg, vbox, backBtn);
//        return root;
//    }
//
//    private static Button createButton(String text, Runnable action) {
//        Button btn = new Button(text);
//        btn.setFont(Font.font("Consolas", 24));
//        btn.setTextFill(Color.web("#e2b0ff"));
//        btn.setPrefWidth(300);
//        btn.setPrefHeight(50);
//        btn.setBackground(new Background(new BackgroundFill(
//                Color.TRANSPARENT, new CornerRadii(30), javafx.geometry.Insets.EMPTY)));
//
//        btn.setBorder(new Border(new BorderStroke(
//                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
//                        new Stop(0, Color.web("#b388ff")),
//                        new Stop(0.5, Color.web("#ffeb3b")),
//                        new Stop(1, Color.web("#b388ff"))),
//                BorderStrokeStyle.SOLID, new CornerRadii(30), new BorderWidths(3))));
//
//        btn.setOnMouseEntered(e -> {
//            btn.setTextFill(Color.web("#2a0845"));
//            btn.setBackground(new Background(new BackgroundFill(
//                    Color.web("#ffeb3b"), new CornerRadii(30), javafx.geometry.Insets.EMPTY)));
//        });
//        btn.setOnMouseExited(e -> {
//            btn.setTextFill(Color.web("#e2b0ff"));
//            btn.setBackground(Background.EMPTY);
//        });
//
//        btn.setOnAction(e -> action.run());
//        return btn;
//    }
//}
