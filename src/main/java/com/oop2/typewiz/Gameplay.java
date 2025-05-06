package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class Gameplay extends GameApplication {

    private String currentInput = "";
    private String targetWord = "";
    private final List<String> words = List.of(
            "cat", "dog", "sun", "moon", "tree",
            "apple", "orange", "banana", "guitar",
            "elephant", "kangaroo", "crocodile",
            "variable", "function", "boolean",
            "tralelo tralala", "bombardillo crocodillo", "chimpanzini bananini"
    );
    private final Random random = new Random();
    private Text wordText;
    private Text inputText;
    private Rectangle healthBar;
    private Text healthText;
    private Text scoreText;
    private Text titleText;
    private Text instructionsText;
    private Text timerText;
    private double timeLeft;
    private double lastTime;
    private boolean timerRunning;

    private static final double BASE_TIME_LIMIT = 5.0;
    private static final double TIME_PENALTY = 1.0;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Typing Master");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    @Override
    protected void initGame() {
        FXGL.getWorldProperties().setValue("score", 0);
        FXGL.getWorldProperties().setValue("health", 100);
        FXGL.getWorldProperties().setValue("streak", 0);
        spawnNewWord();
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addEventHandler(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().isEmpty() && event.getCharacter().charAt(0) >= 32) {
                currentInput += event.getCharacter();
                updateInputDisplay();

                if (!targetWord.startsWith(currentInput)) {
                    decreaseHealth();
                    FXGL.getWorldProperties().setValue("streak", 0);
                }

                checkInput();
            }
        });

        FXGL.getInput().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && currentInput.length() > 0) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                updateInputDisplay();
            }
        });
    }

    private void startTimer() {
        timeLeft = BASE_TIME_LIMIT + (targetWord.length() * 0.2);
        lastTime = FXGL.getGameTimer().getNow();
        timerRunning = true;

        FXGL.run(() -> {
            if (timerRunning) {
                double currentTime = FXGL.getGameTimer().getNow();
                double elapsed = currentTime - lastTime;
                lastTime = currentTime;

                timeLeft -= elapsed;
                updateTimerDisplay();

                if (timeLeft <= 0) {
                    timerRunning = false;
                    decreaseHealth();
                    FXGL.getWorldProperties().setValue("streak", 0);
                    spawnNewWord();
                }
            }
        }, Duration.seconds(0.05));
    }

    private void stopTimer() {
        timerRunning = false;
    }

    private void updateTimerDisplay() {
        if (timerText != null) {
            timerText.setText(String.format("TIME: %.1f", timeLeft));

            if (timeLeft > BASE_TIME_LIMIT * 0.5) {
                timerText.setFill(Color.LIME);
            } else if (timeLeft > BASE_TIME_LIMIT * 0.25) {
                timerText.setFill(Color.YELLOW);
            } else {
                timerText.setFill(Color.RED);
            }
        }
    }

    private void decreaseHealth() {
        int currentHealth = FXGL.getWorldProperties().getInt("health");
        int penalty = Math.max(1, 5 - FXGL.getWorldProperties().getInt("streak") / 5);
        FXGL.getWorldProperties().setValue("health", currentHealth - penalty);

        timeLeft = Math.max(0.5, timeLeft - TIME_PENALTY);
        updateHealthBar();

        if (currentHealth - penalty <= 0) {
            stopTimer();
            FXGL.getDialogService().showMessageBox("Game Over!\nFinal Score: " +
                    FXGL.getWorldProperties().getInt("score") +
                    "\nPress OK to restart", () -> {
                FXGL.getGameController().startNewGame();
            });
        }
    }

    private void updateHealthBar() {
        int currentHealth = FXGL.getWorldProperties().getInt("health");
        double healthPercentage = Math.max(0.0, currentHealth / 100.0);  // Clamp to [0.0, 1.0]

        healthBar.setWidth(200 * healthPercentage);

        if (healthPercentage > 0.6) {
            healthBar.setFill(Color.LIME);
        } else if (healthPercentage > 0.3) {
            double factor = (healthPercentage - 0.3) / 0.3;
            factor = Math.max(0.0, Math.min(1.0, factor));  // Clamp to [0.0, 1.0]
            healthBar.setFill(Color.color(1.0, 0.5 + factor * 0.5, 0.0));
        } else {
            double factor = healthPercentage / 0.3;
            factor = Math.max(0.0, Math.min(1.0, factor));  // Clamp to [0.0, 1.0]
            healthBar.setFill(Color.color(1.0, factor * 0.5, 0.0));
        }
    }


    private void spawnNewWord() {
        stopTimer();
        FXGL.getGameWorld().getEntitiesCopy().forEach(e -> e.removeFromWorld());

        targetWord = words.get(random.nextInt(words.size()));
        currentInput = "";

        wordText = new Text(targetWord);
        wordText.setFont(Font.font("Arial", 48));
        wordText.setFill(Color.LIGHTGRAY);
        wordText.setStroke(Color.DARKGRAY);
        wordText.setStrokeWidth(1);

        inputText = new Text();
        inputText.setFont(Font.font("Arial", 48));
        inputText.setFill(Color.WHITE);

        FXGL.entityBuilder()
                .at(FXGL.getAppWidth()/2 - wordText.getLayoutBounds().getWidth()/2,
                        FXGL.getAppHeight()/2 - 30)
                .view(wordText)
                .buildAndAttach();

        FXGL.entityBuilder()
                .at(FXGL.getAppWidth()/2 - wordText.getLayoutBounds().getWidth()/2,
                        FXGL.getAppHeight()/2 + 50)
                .view(inputText)
                .buildAndAttach();

        startTimer();
    }

    private void updateInputDisplay() {
        inputText.setText(currentInput);

        if (targetWord.startsWith(currentInput)) {
            double completion = (double) currentInput.length() / targetWord.length();
            inputText.setFill(Color.color(1 - completion * 0.5, 1.0, 1 - completion * 0.5));
        } else {
            inputText.setFill(Color.ORANGERED);
        }
    }

    private void checkInput() {
        if (currentInput.equals(targetWord)) {
            int streak = FXGL.getWorldProperties().getInt("streak");
            int scoreBonus = 1 + streak / 3;

            FXGL.getWorldProperties().increment("score", scoreBonus);
            FXGL.getWorldProperties().increment("streak", 1);

            wordText.setFill(Color.GOLD);
            inputText.setFill(Color.LIME);

            FXGL.runOnce(this::spawnNewWord, Duration.seconds(0.2));
        }
    }

    @Override
    protected void initUI() {
        // Game title
        titleText = new Text("TYPING WIZARD!");
        titleText.setFont(Font.font("Arial", 36));
        titleText.setFill(Color.GOLD);
        titleText.setStroke(Color.DARKGOLDENROD);
        titleText.setStrokeWidth(1);
        titleText.setTranslateX(FXGL.getAppWidth()/2 - titleText.getLayoutBounds().getWidth()/2);
        titleText.setTranslateY(50);

        // Instructions
        instructionsText = new Text("Type the words as they appear. Backspace to correct mistakes.");
        instructionsText.setFont(Font.font("Arial", 16));
        instructionsText.setFill(Color.LIGHTGRAY);
        instructionsText.setTranslateX(FXGL.getAppWidth()/2 - instructionsText.getLayoutBounds().getWidth()/2);
        instructionsText.setTranslateY(90);

        // Score display
        scoreText = new Text();
        scoreText.setFont(Font.font("Arial", 24));
        scoreText.setFill(Color.WHITE);
        scoreText.setTranslateX(20);
        scoreText.setTranslateY(40);

        scoreText.textProperty().bind(
                FXGL.getWorldProperties().intProperty("score").asString("SCORE: %d")
        );

        // Streak display
        Text streakText = new Text();
        streakText.setFont(Font.font("Arial", 18));
        streakText.setFill(Color.CYAN);
        streakText.setTranslateX(20);
        streakText.setTranslateY(70);

        streakText.textProperty().bind(
                FXGL.getWorldProperties().intProperty("streak").asString("STREAK: %d")
        );

        // Timer display
        timerText = new Text("TIME: 0.0");
        timerText.setFont(Font.font("Arial", 18));
        timerText.setFill(Color.LIME);
        timerText.setTranslateX(20);
        timerText.setTranslateY(100);

        // Health bar background
        Rectangle healthBarBg = new Rectangle(220, 26);
        healthBarBg.setFill(Color.rgb(30, 30, 30));
        healthBarBg.setStroke(Color.rgb(80, 80, 80));
        healthBarBg.setArcWidth(10);
        healthBarBg.setArcHeight(10);
        healthBarBg.setTranslateX(15);
        healthBarBg.setTranslateY(130);

        // Health bar
        healthBar = new Rectangle(200, 20);
        healthBar.setFill(Color.LIME);
        healthBar.setTranslateX(20);
        healthBar.setTranslateY(133);
        healthBar.setArcWidth(8);
        healthBar.setArcHeight(8);

        // Health text
        healthText = new Text();
        healthText.setFont(Font.font("Arial", 18));
        healthText.setFill(Color.WHITE);
        healthText.setTranslateX(230);
        healthText.setTranslateY(150);

        healthText.textProperty().bind(
                FXGL.getWorldProperties().intProperty("health").asString("%d%%")
        );

        // Add all UI elements
        FXGL.getGameScene().addUINode(titleText);
        FXGL.getGameScene().addUINode(instructionsText);
        FXGL.getGameScene().addUINode(scoreText);
        FXGL.getGameScene().addUINode(streakText);
        FXGL.getGameScene().addUINode(timerText);
        FXGL.getGameScene().addUINode(healthBarBg);
        FXGL.getGameScene().addUINode(healthBar);
        FXGL.getGameScene().addUINode(healthText);

        FXGL.getGameScene().setBackgroundColor(Color.rgb(20, 20, 30));
    }

    public static void main(String[] args) {
        launch(args);
    }
}