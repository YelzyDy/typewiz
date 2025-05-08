package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.shape.Line;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;

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

    // Character entities
    private Entity playerEntity;
    private Entity enemyEntity;
    private Rectangle enemyHealthBar;
    private Entity enemyHealthBarEntity;
    private double enemyHealth = 100;
    private static final double DAMAGE_PER_HIT = 10;

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

        // Create player character
        Rectangle playerRect = new Rectangle(50, 80, Color.BLUE);
        playerEntity = FXGL.entityBuilder()
                .at(100, FXGL.getAppHeight() / 2 - 40)
                .view(playerRect)
                .buildAndAttach();

        // Create enemy character
        Rectangle enemyRect = new Rectangle(50, 80, Color.RED);
        enemyEntity = FXGL.entityBuilder()
                .at(FXGL.getAppWidth() - 150, FXGL.getAppHeight() / 2 - 40)
                .view(enemyRect)
                .buildAndAttach();

        // Create enemy health bar
        enemyHealthBar = new Rectangle(100, 10, Color.LIME);
        enemyHealthBarEntity = FXGL.entityBuilder()
                .at(FXGL.getAppWidth() - 175, FXGL.getAppHeight() / 2 - 60)
                .view(enemyHealthBar)
                .buildAndAttach();

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
                    playMistakeAnimation();
                    enemyShootWizardBeam();
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

        // Only remove entities that are not player or enemy
        FXGL.getGameWorld().getEntitiesCopy().forEach(e -> {
            if (e != playerEntity && e != enemyEntity && e != enemyHealthBarEntity) {
                e.removeFromWorld();
            }
        });

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

    private void playMistakeAnimation() {
        // Create mistake particles
        int particleCount = 15;
        double centerX = FXGL.getAppWidth() / 2;
        double centerY = FXGL.getAppHeight() / 2;

        for (int i = 0; i < particleCount; i++) {
            Rectangle particle = new Rectangle(2, 2);

            // Red/Orange colors for mistakes
            Color color = Color.rgb(255, 50 + random.nextInt(100), 0);
            particle.setFill(color);

            // Random position around the input text
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * 30;
            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance + 50; // Offset to be near input text

            FXGL.entityBuilder()
                    .at(x, y)
                    .view(particle)
                    .with(new ParticleBehavior(true)) // true for mistake particles
                    .buildAndAttach();
        }
    }

    private void shootBeam() {
        // Create beam line
        Line beam = new Line(
                playerEntity.getX() + 50,  // Start from right side of player
                playerEntity.getY() + 40,  // Middle of player
                enemyEntity.getX(),        // To left side of enemy
                enemyEntity.getY() + 40    // Middle of enemy
        );
        beam.setStroke(Color.LIME);
        beam.setStrokeWidth(15);       // Much thicker beam
        beam.setOpacity(0.9);

        // Add dramatic glow effect
        beam.setEffect(new javafx.scene.effect.Glow(0.8));

        Entity beamEntity = FXGL.entityBuilder()
                .view(beam)
                .buildAndAttach();

        // Animate the beam with more dramatic timing
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.6), beam);
        fadeOut.setFromValue(0.9);
        fadeOut.setToValue(0);

        ScaleTransition stretch = new ScaleTransition(Duration.seconds(0.6), beam);
        stretch.setFromX(0);
        stretch.setToX(1.2);  // Overshoot a bit for drama

        ParallelTransition animation = new ParallelTransition(fadeOut, stretch);
        animation.setOnFinished(e -> beamEntity.removeFromWorld());
        animation.play();

        // Add dramatic particles along the beam
        addDramaticBeamParticles(beam, Color.LIME, Color.GREEN, Color.YELLOWGREEN);

        // Damage enemy
        enemyHealth -= DAMAGE_PER_HIT;
        updateEnemyHealthBar();

        if (enemyHealth <= 0) {
            handleEnemyDefeat();
        }
    }

    private void addDramaticBeamParticles(Line beam, Color... colors) {
        int particleCount = 40;  // More particles for more dramatic effect

        for (int i = 0; i < particleCount; i++) {
            // Position along the beam line
            double ratio = random.nextDouble();
            double x = beam.getStartX() + ratio * (beam.getEndX() - beam.getStartX());
            double y = beam.getStartY() + ratio * (beam.getEndY() - beam.getStartY());

            // Small random offset from the line
            double offsetAngle = random.nextDouble() * Math.PI * 2;
            double offsetDistance = random.nextDouble() * 20;  // Wider spread
            x += Math.cos(offsetAngle) * offsetDistance;
            y += Math.sin(offsetAngle) * offsetDistance;

            // Larger, more dramatic particles
            Rectangle particle = new Rectangle(4 + random.nextInt(4), 4 + random.nextInt(4));

            // Random color from provided colors
            Color color = colors[random.nextInt(colors.length)];
            particle.setFill(color);

            // Add glow to some particles
            if (random.nextBoolean()) {
                particle.setEffect(new javafx.scene.effect.Glow(0.7));
            }

            FXGL.entityBuilder()
                    .at(x, y)
                    .view(particle)
                    .with(new ParticleBehavior(false))  // Use success particles behavior
                    .buildAndAttach();
        }
    }

    private void updateEnemyHealthBar() {
        double healthPercentage = Math.max(0, enemyHealth / 100.0);
        enemyHealthBar.setWidth(100 * healthPercentage);

        if (healthPercentage > 0.6) {
            enemyHealthBar.setFill(Color.LIME);
        } else if (healthPercentage > 0.3) {
            enemyHealthBar.setFill(Color.YELLOW);
        } else {
            enemyHealthBar.setFill(Color.RED);
        }
    }

    private void handleEnemyDefeat() {
        // Add victory effects
        playExplosionAnimation();

        // Reset enemy health
        enemyHealth = 100;
        updateEnemyHealthBar();

        // Increase score
        FXGL.getWorldProperties().increment("score", 50);
    }

    private void playExplosionAnimation() {
        // Create explosion particles around the enemy
        int particleCount = 30 + targetWord.length() * 5;
        double centerX = enemyEntity.getX() + 25; // Center of enemy
        double centerY = enemyEntity.getY() + 40;

        for (int i = 0; i < particleCount; i++) {
            Rectangle particle = new Rectangle(3, 3);

            Color color = Color.rgb(
                    200 + random.nextInt(55),  // More red for hit effect
                    200 + random.nextInt(55),  // Some green
                    100 + random.nextInt(50)   // Less blue
            );

            particle.setFill(color);

            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * 50;
            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance;

            FXGL.entityBuilder()
                    .at(x, y)
                    .view(particle)
                    .with(new ParticleBehavior(false))
                    .buildAndAttach();
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

            // Shoot beam at enemy
            shootBeam();

            // Play explosion animation
            playExplosionAnimation();

            FXGL.runOnce(this::spawnNewWord, Duration.seconds(0.5));
        }
    }

    private void enemyShootWizardBeam() {
        // Create wizard beam line
        Line wizardBeam = new Line(
                enemyEntity.getX(),        // Start from left side of enemy
                enemyEntity.getY() + 40,   // Middle of enemy
                playerEntity.getX() + 50,  // To right side of player
                playerEntity.getY() + 40   // Middle of player
        );

        // Magical colors for wizard beam
        wizardBeam.setStroke(Color.PURPLE);
        wizardBeam.setStrokeWidth(18);  // Even thicker for wizard beam
        wizardBeam.setOpacity(0.9);
        wizardBeam.getStrokeDashArray().addAll(15.0, 10.0); // Dashed line for magical effect

        // Add dramatic glow effect
        wizardBeam.setEffect(new javafx.scene.effect.Glow(1.0));

        Entity beamEntity = FXGL.entityBuilder()
                .view(wizardBeam)
                .buildAndAttach();

        // Animate the beam with even more dramatic wizardly effects
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.7), wizardBeam);
        fadeOut.setFromValue(0.9);
        fadeOut.setToValue(0);

        ScaleTransition stretch = new ScaleTransition(Duration.seconds(0.7), wizardBeam);
        stretch.setFromX(0);
        stretch.setToX(1.3);  // More overshoot for wizard drama

        ParallelTransition animation = new ParallelTransition(fadeOut, stretch);
        animation.setOnFinished(e -> beamEntity.removeFromWorld());
        animation.play();

        // Add magical particles along the beam
        addDramaticBeamParticles(wizardBeam, Color.PURPLE, Color.MAGENTA, Color.BLUE);
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

class ParticleBehavior extends com.almasb.fxgl.entity.component.Component {
    private double velocityX;
    private double velocityY;
    private double lifeTime;
    private double currentTime = 0.0;
    private double fadeStart;
    private boolean isMistake;

    public ParticleBehavior(boolean isMistake) {
        Random random = new Random();
        this.isMistake = isMistake;

        if (isMistake) {
            // Faster, shorter-lived particles for mistakes
            velocityX = (random.nextDouble() - 0.5) * 300;
            velocityY = (random.nextDouble() - 0.5) * 300;
            lifeTime = 0.5;
            fadeStart = 0.3;
        } else {
            // Normal success particles
            velocityX = (random.nextDouble() - 0.5) * 400;
            velocityY = (random.nextDouble() - 0.5) * 400;
            lifeTime = 1.0;
            fadeStart = 0.7;
        }
    }

    @Override
    public void onUpdate(double tpf) {
        currentTime += tpf;

        // Update position
        entity.translateX(velocityX * tpf);
        entity.translateY(velocityY * tpf);


        // Apply gravity
        velocityY += 100 * tpf;

        // Fade out
        if (currentTime > fadeStart) {
            double opacity = 1.0 - ((currentTime - fadeStart) / (lifeTime - fadeStart));
            entity.getViewComponent().setOpacity(opacity);
        }

        // Remove when expired
        if (currentTime >= lifeTime) {
            entity.removeFromWorld();
        }
    }
}