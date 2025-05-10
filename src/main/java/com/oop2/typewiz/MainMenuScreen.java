package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.util.ThreadManager;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainMenuScreen extends FXGLMenu {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public MainMenuScreen() {
        super(MenuType.MAIN_MENU);

        // Main container with magical gradient background
        StackPane root = new StackPane();
        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2a0845, #4a148c);");

        // Glass panel effect (matches difficulty screen)
        Rectangle panel = new Rectangle(600, 700); // was 500 x 550
        panel.setArcHeight(30);
        panel.setArcWidth(30);
        panel.setFill(Color.web("rgba(60, 0, 90, 0.5)"));
        panel.setStroke(Color.web("#b388ff"));
        panel.setStrokeWidth(2);
        panel.setEffect(new DropShadow(20, Color.web("#ffeb3b", 0.3)));

        // Game title with magical effects (Bouncing Animation)
        Text title = new Text("TYPEWIZ");
        title.setFont(Font.font("Papyrus", FontWeight.EXTRA_BOLD, 72));
        title.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#b388ff")),
                new Stop(0.5, Color.web("#ffeb3b")),
                new Stop(1, Color.web("#b388ff"))));
        title.setEffect(new Glow(0.8));

//        // Bouncing animation for title
//        ThreadManager.runAsyncThenUI(
//                () -> {
//                    TranslateTransition bounce = new TranslateTransition(Duration.seconds(1), title);
//                    bounce.setByY(-20); // Move up
//                    bounce.setCycleCount(Animation.INDEFINITE);
//                    bounce.setAutoReverse(true);
//                    bounce.setInterpolator(Interpolator.EASE_BOTH);
//                    bounce.play();
//                },
//                () -> {} // UI thread callback (empty since animation starts asynchronously)
//        );

        // Subtitle with typewriter effect (Fade-In Animation)
        Text subtitle = new Text("Master the Magic of Typing");
        subtitle.setFont(Font.font("Consolas", 24));
        subtitle.setFill(Color.web("#d1c4e9"));
        subtitle.setEffect(new DropShadow(5, Color.web("#7e57c2")));

        // Fade-in animation for subtitle
        ThreadManager.runAsyncThenUI(
                () -> {
                    FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), subtitle);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                },
                () -> {} // UI thread callback
        );

        // Buttons with wizard theme
        Button startButton = createWizardButton("START QUEST", () -> {
            executorService.submit(() -> {
                // Simulate asset loading
                try {
                    Thread.sleep(300);  // Simulate background loading
                } catch (InterruptedException ignored) {}

                // Once the assets are loaded, update the UI
                FXGL.runOnce(() -> {
                    DifficultyMenuScreen screen = new DifficultyMenuScreen(
                            () -> FXGL.getSceneService().popSubScene(),
                            () -> runStartGameThread(Difficulty.APPRENTICE),
                            () -> runStartGameThread(Difficulty.WIZARD),
                            () -> runStartGameThread(Difficulty.ARCHMAGE)
                    );
                    FXGL.getSceneService().pushSubScene(screen);
                }, Duration.seconds(0));
            });
        });

        Button helpButton = createWizardButton("SPELLBOOK", () -> {
            FXGL.getDialogService().showMessageBox(
                    "~ MAGIC TYPING RULES ~\n\n" +
                            "1. Type the incantations as they appear\n" +
                            "2. Complete them before time runs out\n" +
                            "3. Accuracy increases your mana\n\n" +
                            "Press any key to begin your training!",
                    () -> {}
            );
        });

        Button activityButton = createWizardButton("CREATORS", () -> {
            FXGL.getSceneService().pushSubScene(new CreditsScreen(() -> FXGL.getSceneService().popSubScene()));
        });

        Button exitButton = createWizardButton("LEAVE TOWER", () -> {
            FXGL.getGameController().exit();
        });

        // Layout
        VBox menuBox = new VBox(20, title, subtitle, startButton, helpButton, activityButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);

        // Menu Box Fade and Scale Animation
        menuBox.setOpacity(0);
        ThreadManager.runAsyncThenUI(
                () -> {
                    ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(1), menuBox);
                    scaleIn.setToX(1);
                    scaleIn.setToY(1);

                    FadeTransition fadeInMenu = new FadeTransition(Duration.seconds(1), menuBox);
                    fadeInMenu.setFromValue(0);
                    fadeInMenu.setToValue(1);

                    ParallelTransition parallelTransition = new ParallelTransition(fadeInMenu, scaleIn);
                    parallelTransition.play();
                },
                () -> {} // UI thread callback
        );

        // Glass panel with menu box inside
        StackPane glassPane = new StackPane(panel, menuBox);
        root.getChildren().add(glassPane);
        getContentRoot().getChildren().add(root);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
    
    private void runStartGameThread(Difficulty difficulty) {
        ThreadManager.runAsyncThenUI(
                () -> {
                    FXGL.getWorldProperties().setValue("difficulty", difficulty);

                    // Simulate preloading sounds/resources
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {}
                },
                () -> FXGL.getGameController().startNewGame()
        );
    }

    private void preloadAssetsInBackground() {
        // Simulate loading assets
        try {
            Thread.sleep(300); // Replace with actual preload code like FXGL.getAssetLoader().load...
        } catch (InterruptedException ignored) {}
    }

    private Button createWizardButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font("Papyrus", FontWeight.BOLD, 28));
        button.setTextFill(Color.web("#e2b0ff"));
        button.setPrefWidth(350);
        button.setPrefHeight(70);

        // Set default transparent background
        button.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT,
                new CornerRadii(10),
                javafx.geometry.Insets.EMPTY
        )));

        // Gradient border
        button.setBorder(new Border(new BorderStroke(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#b388ff")),
                        new Stop(0.5, Color.web("#ffeb3b")),
                        new Stop(1, Color.web("#b388ff"))),
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

    private void startGame(Difficulty difficulty) {
        FXGL.getWorldProperties().setValue("difficulty", difficulty);
        FXGL.getGameController().startNewGame();
    }
}
