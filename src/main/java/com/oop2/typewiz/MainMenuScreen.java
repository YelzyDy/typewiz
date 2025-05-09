package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.Button;

public class MainMenuScreen extends FXGLMenu {

    public MainMenuScreen() {
        super(MenuType.MAIN_MENU);

        // Main container with magical gradient background
        StackPane root = new StackPane();
        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2a0845, #4a148c);");

        // Glass panel effect (matches difficulty screen)
        Rectangle panel = new Rectangle(500, 550);
        panel.setArcHeight(30);
        panel.setArcWidth(30);
        panel.setFill(Color.web("rgba(60, 0, 90, 0.5)"));
        panel.setStroke(Color.web("#b388ff"));
        panel.setStrokeWidth(2);
        panel.setEffect(new DropShadow(20, Color.web("#ffeb3b", 0.3)));

        // Game title with magical effects
        Text title = new Text("TYPEWIZ");
        title.setFont(Font.font("Papyrus", FontWeight.EXTRA_BOLD, 72));
        title.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#b388ff")),
                new Stop(0.5, Color.web("#ffeb3b")),
                new Stop(1, Color.web("#b388ff"))));
        title.setEffect(new Glow(0.8));

        // Subtitle with typewriter effect
        Text subtitle = new Text("Master the Magic of Typing");
        subtitle.setFont(Font.font("Consolas", 24));
        subtitle.setFill(Color.web("#d1c4e9"));
        subtitle.setEffect(new DropShadow(5, Color.web("#7e57c2")));

        // Buttons with wizard theme
        Button startButton = createWizardButton("START QUEST", () -> {
            new Thread(() -> {
                // Simulate asset preloading
                preloadAssetsInBackground();

                // Create difficulty menu on FX thread
                javafx.application.Platform.runLater(() -> {
                    DifficultyMenuScreen screen = new DifficultyMenuScreen(
                            () -> FXGL.getSceneService().popSubScene(),
                            () -> runStartGameThread(Difficulty.APPRENTICE),
                            () -> runStartGameThread(Difficulty.WIZARD),
                            () -> runStartGameThread(Difficulty.ARCHMAGE)
                    );
                    FXGL.getSceneService().pushSubScene(screen);
                });
            }).start();
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

        Button exitButton = createWizardButton("LEAVE TOWER", () -> {
            FXGL.getGameController().exit();
        });

        // Layout
        VBox menuBox = new VBox(20, title, subtitle, startButton, helpButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);

        StackPane glassPane = new StackPane(panel, menuBox);
        root.getChildren().add(glassPane);
        getContentRoot().getChildren().add(root);
    }
    private void runStartGameThread(Difficulty difficulty) {
        new Thread(() -> {
            FXGL.getWorldProperties().setValue("difficulty", difficulty);

            Thread soundThread = new Thread(() -> {
                // Simulate sound preloading or other resources
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            });

            soundThread.start();

            // Wait for soundThread to finish
            try {
                soundThread.join();
            } catch (InterruptedException ignored) {}

            // Start game on FX thread
            javafx.application.Platform.runLater(() -> {
                FXGL.getGameController().startNewGame();
            });
        }).start();
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

        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setBackground(new Background(new BackgroundFill(
                    Color.web("rgba(179, 136, 255, 0.1)"),
                    new CornerRadii(10),
                    javafx.geometry.Insets.EMPTY
            )));
            button.setEffect(new Glow(0.3));
        });

        button.setOnMouseExited(e -> {
            button.setBackground(new Background(new BackgroundFill(
                    Color.TRANSPARENT,
                    new CornerRadii(10),
                    javafx.geometry.Insets.EMPTY
            )));
            button.setEffect(null);
        });

        button.setOnAction(e -> action.run());

        return button;
    }


    private void startGame(Difficulty difficulty) {
        FXGL.getWorldProperties().setValue("difficulty", difficulty);
        FXGL.getGameController().startNewGame();
    }
}





//package com.oop2.typewiz;
//
//import com.almasb.fxgl.app.scene.FXGLMenu;
//import com.almasb.fxgl.app.scene.MenuType;
//import com.almasb.fxgl.dsl.FXGL;
//import javafx.animation.FadeTransition;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.effect.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.*;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.scene.text.Text;
//import javafx.scene.control.Button;
//import javafx.util.Duration;
//
//public class MainMenuScreen extends FXGLMenu {
//
//    public MainMenuScreen() {
//        super(MenuType.MAIN_MENU);
//
//        // Main container with magical gradient
//        StackPane root = new StackPane();
//        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
//
//        // Enchanted background (deep violet with yellow sparkles)
//        Rectangle background = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
//        background.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
//                new Stop(0, Color.web("#2a0845")),  // Deep purple
//                new Stop(0.5, Color.web("#4a148c")),  // Royal purple
//                new Stop(1, Color.web("#1a0638"))));  // Darkest purple
//
//        // Add magical glow to background
//        background.setEffect(new Bloom(0.1));
//
////        // Create twinkling stars (yellow particles)
//////        Pane stars = createStarParticles(50);
////        root.getChildren().addAll(background, stars);
//
//        root.getChildren().addAll(background);
//
//        // Glass panel for menu items
//        Rectangle menuPanel = new Rectangle(500, 500);
//        menuPanel.setArcHeight(30);
//        menuPanel.setArcWidth(30);
//        menuPanel.setFill(Color.web("rgba(60, 0, 90, 0.5)"));
//        menuPanel.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
//                new Stop(0, Color.web("#b388ff")),  // Violet
//                new Stop(0.5, Color.web("#ffeb3b")),  // Yellow
//                new Stop(1, Color.web("#b388ff"))));  // Violet
//        menuPanel.setStrokeWidth(3);
//        menuPanel.setEffect(new DropShadow(30, Color.web("#ffeb3b", 0.3)));
//
//        // VBox for menu items
//        VBox menuBox = new VBox(20);
//        menuBox.setAlignment(Pos.CENTER);
//        menuBox.setMaxWidth(400);
//
//        // Game title with wizard-themed effects
//        Text title = new Text("TYPEWIZ");
//        title.setFont(Font.font("Papyrus", FontWeight.EXTRA_BOLD, 72));
//
//        // Gradient text (violet to yellow)
//        title.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
//                new Stop(0, Color.web("#b388ff")),  // Light violet
//                new Stop(0.5, Color.web("#ffeb3b")),  // Bright yellow
//                new Stop(1, Color.web("#b388ff"))));  // Light violet
//
//        // Magical text effects
//        title.setEffect(new Blend(
//                BlendMode.ADD,
//                new DropShadow(15, Color.web("#ffeb3b", 0.7)),  // Yellow glow
//                new InnerShadow(10, Color.web("#4a148c"))  // Purple inner shadow
//        ));
//
//        // Subtitle with typewriter effect
//        Text subtitle = new Text("_");
//        subtitle.setFont(Font.font("Consolas", 20));
//        subtitle.setFill(Color.web("#d1c4e9"));
//        animateTypingText(subtitle, "Master the Magic of Typing");
//
//        // Buttons with instant hover effects
//        Button startButton = createWizardButton("START QUEST", () -> {
//            SceneManager.showScreen(TypeWizApp.ScreenType.DIFFICULTY_SELECTION);
//        });
//
//        Button helpButton = createWizardButton("SPELLBOOK", () -> {
//            FXGL.getDialogService().showMessageBox(
//                    "~ MAGIC TYPING RULES ~\n\n" +
//                            "1. Type the incantations (words) as they appear\n" +
//                            "2. Complete them before time runs out\n" +
//                            "3. Accuracy increases your mana (score)\n\n" +
//                            "Press any key to begin your wizard training!",
//                    () -> {}
//            );
//        });
//
//        Button exitButton = createWizardButton("LEAVE TOWER", () -> {
//            FXGL.getGameController().exit();
//        });
//
//        // Add all elements
//        menuBox.getChildren().addAll(title, subtitle, startButton, helpButton, exitButton);
//
//        // Create glass pane effect
//        StackPane glassPane = new StackPane(menuPanel, menuBox);
//        glassPane.setEffect(new Bloom(0.3));
//        root.getChildren().add(glassPane);
//
//        getContentRoot().getChildren().add(root);
//    }
//
////    private Pane createStarParticles(int count) {
////        Pane stars = new Pane();
////        for (int i = 0; i < count; i++) {
////            Rectangle star = new Rectangle(2, 2, Color.web("#ffeb3b", 0.7));
////            star.setLayoutX(Math.random() * FXGL.getAppWidth());
////            star.setLayoutY(Math.random() * FXGL.getAppHeight());
////            star.setEffect(new Glow(0.8));
////
////            // Make stars twinkle
////            FadeTransition ft = new FadeTransition(Duration.seconds(2 + Math.random() * 3), star);
////            ft.setFromValue(0.3);
////            ft.setToValue(0.9);
////            ft.setCycleCount(FadeTransition.INDEFINITE);
////            ft.setAutoReverse(true);
////            ft.play();
////
////            stars.getChildren().add(star);
////        }
////        return stars;
////    }
//
//    private void animateTypingText(Text textNode, String fullText) {
//        final int[] i = new int[1];
//        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
//        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
//                Duration.millis(100),
//                event -> {
//                    if (i[0] <= fullText.length()) {
//                        textNode.setText(fullText.substring(0, i[0]) + (i[0] % 2 == 0 ? "_" : ""));
//                        i[0]++;
//                    }
//                }
//        );
//        timeline.getKeyFrames().add(keyFrame);
//        timeline.setCycleCount(fullText.length());
//        timeline.play();
//    }
//
//    private Button createWizardButton(String text, Runnable action) {
//        Button button = new Button(text);
//        button.setFont(Font.font("Consolas", 28));
//        button.setTextFill(Color.web("#e2b0ff"));
//        button.setPrefWidth(320);
//        button.setPrefHeight(60);
//        button.setBackground(Background.EMPTY);
//
//
//        // Hover effects
//        button.setOnMouseEntered(e -> {
//            button.setTextFill(Color.web("#e94560"));
//            button.setBackground(new Background(new BackgroundFill(
//                    Color.web("rgba(233, 69, 96, 0.1)"),
//                    new CornerRadii(5),
//                    javafx.geometry.Insets.EMPTY
//            )));
//        });
//
//        button.setOnMouseExited(e -> {
//            button.setTextFill(Color.WHITE);
//            button.setBackground(new Background(new BackgroundFill(
//                    Color.TRANSPARENT,
//                    new CornerRadii(5),
//                    javafx.geometry.Insets.EMPTY
//            )));
//        });
//
//        // Magical border (violet to yellow gradient)
//        button.setBorder(new Border(new BorderStroke(
//                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
//                        new Stop(0, Color.web("#b388ff")),
//                        new Stop(0.5, Color.web("#ffeb3b")),
//                        new Stop(1, Color.web("#b388ff"))),
//                BorderStrokeStyle.SOLID,
//                new CornerRadii(30),
//                new BorderWidths(3)
//        )));
//
//        // Transparent background by default
//        button.setBackground(new Background(new BackgroundFill(
//                Color.TRANSPARENT,
//                new CornerRadii(30),
//                javafx.geometry.Insets.EMPTY
//        )));
//
//        // Pre-cache for performance
//        button.setCache(true);
//        button.setCacheHint(javafx.scene.CacheHint.SPEED);
//
//        // Instant hover effects (no lag)
//        button.setOnMouseEntered(e -> {
//            button.setTextFill(Color.web("#2a0845"));  // Dark purple
//            button.setBackground(new Background(new BackgroundFill(
//                    new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
//                            new Stop(0, Color.web("#b388ff")),
//                            new Stop(0.5, Color.web("#ffeb3b")),
//                            new Stop(1, Color.web("#b388ff"))),
//                    new CornerRadii(30),
//                    javafx.geometry.Insets.EMPTY
//            )));
//            button.setEffect(new DropShadow(15, Color.web("#ffeb3b", 0.7)));
//        });
//
//        button.setOnMouseExited(e -> {
//            button.setTextFill(Color.web("#e2b0ff"));
//            button.setBackground(new Background(new BackgroundFill(
//                    Color.TRANSPARENT,
//                    new CornerRadii(30),
//                    javafx.geometry.Insets.EMPTY
//            )));
//            button.setEffect(null);
//        });
//
//        button.setOnAction(e -> {
//            // Sparkle effect on click
//            FadeTransition ft = new FadeTransition(Duration.millis(200), button);
//            ft.setFromValue(1.0);
//            ft.setToValue(0.7);
//            ft.setCycleCount(2);
//            ft.setAutoReverse(true);
//            ft.setOnFinished(event -> action.run());
//            ft.play();
//        });
//
//        return button;
//    }
//}