package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class HowToPlayScreen extends FXGLMenu {

    public HowToPlayScreen(Runnable backAction) {
        super(MenuType.GAME_MENU);

        // Set up the background
        Pane background = new Pane();
        background.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        background.setStyle(
                "-fx-background-image: url('/assets/textures/background-and-platforms/spellbookbg.png');" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        // Title: "How to Play"
        Text title = new Text("How to Play");
        title.setFont(javafx.scene.text.Font.font("Papyrus", 50));
        title.setFill(Color.web("#ffeb3b"));
        title.setEffect(new Glow(0.8));

        // Container for instructions
        VBox instructionsContainer = new VBox(20);
        instructionsContainer.setAlignment(Pos.TOP_LEFT);  // Left justify text within the container
        instructionsContainer.setPrefWidth(600);
        instructionsContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 20;");

        // Instructions with numbering
        String[] instructions = {
                "TypeWiz is a typing game where you, a wizard, battle monsters.",
                "Each monster has a word you need to type to kill it.",
                "Press spacebar to cast a spell and eliminate the monster.",
                "Use Shift to change your target monster.",
                "If a monster reaches the wizard tower, your health will decrease.",
                "If your health runs out, it's game over.",
                "Press Enter to retry after a game over.",
                "Master your typing skills to survive waves of monsters!"
        };

        for (int i = 0; i < instructions.length; i++) {
            // Create a new HBox for each instruction with number
            HBox itemBox = new HBox(10);
            Text numberText = new Text((i + 1) + ". ");
            numberText.setFont(javafx.scene.text.Font.font("Papyrus", 24));
            numberText.setFill(Color.web("#ffeb3b"));
            numberText.setEffect(new Glow(0.6));

            Text instructionText = new Text(instructions[i]);
            instructionText.setFont(javafx.scene.text.Font.font("Papyrus", 24));
            instructionText.setFill(Color.web("#ffeb3b"));
            instructionText.setEffect(new Glow(0.6));

            itemBox.getChildren().addAll(numberText, instructionText);
            instructionsContainer.getChildren().add(itemBox);
        }

        // Create a magical "Back" button
        Text backButton = new Text("Back to the Tower");
        backButton.setFont(javafx.scene.text.Font.font("Papyrus", 28));
        backButton.setFill(Color.web("#ffeb3b"));
        backButton.setEffect(new Glow(0.8));

        // Add hover effect to back button
        backButton.setOnMouseEntered(event -> {
            backButton.setFill(Color.web("#b388ff")); // Change color on hover
        });

        backButton.setOnMouseExited(event -> {
            backButton.setFill(Color.web("#ffeb3b")); // Reset color when hover ends
        });

        // On-click event for the Back button
        backButton.setOnMouseClicked(event -> {
            FXGL.play("sound-library/click.wav");
            // Transition back to the main menu
            FXGL.getGameScene().removeUINode(getContentRoot());
            FXGL.getGameScene().getContentRoot().getChildren().add(new MainMenuScreen().getContentRoot());
        });

        // Wrapper to center the instructionsContainer while keeping text left-aligned
        VBox instructionsWrapper = new VBox(instructionsContainer);
        instructionsWrapper.setAlignment(Pos.CENTER); // Center the VBox itself
        instructionsContainer.setMaxWidth(600);
        instructionsWrapper.setMaxWidth(600);

        instructionsContainer.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.5); " +
                        "-fx-padding: 20; " +
                        "-fx-background-radius: 20;"
        );

// Arrange the title, instructionsWrapper, and backButton in a VBox
        VBox centerBox = new VBox(30, title, instructionsWrapper, backButton);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(700); // <— Set max width to prevent stretching



        // Set up the root StackPane
        StackPane root = new StackPane(background, centerBox);
        getContentRoot().getChildren().setAll(root);

        // Fade in the whole centerBox
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), centerBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

// Floating animation for the instructionsContainer
        TranslateTransition floatEffect = new TranslateTransition(Duration.seconds(2.5), instructionsWrapper);
        floatEffect.setByY(15);
        floatEffect.setAutoReverse(true);
        floatEffect.setCycleCount(Animation.INDEFINITE);
        floatEffect.play();

// Slight scale animation for the backButton to give it a magical pulse
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), backButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();


        // Play magical music or sound effect (optional)
//        FXGL.getGameTimer().runOnceAfter(() -> {
//            FXGL.getAudioPlayer().playMusic(FXGL.getAssetLoader().loadMusic("magic_theme.wav"));
//        }, Duration.seconds(1)); // Start the music after 1 second delay
    }
}
