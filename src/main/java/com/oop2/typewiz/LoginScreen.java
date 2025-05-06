package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoginScreen extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("TypeWiz Login");
        settings.setWidth(1550);
        settings.setHeight(800);
        settings.setMainMenuEnabled(false);
        settings.setGameMenuEnabled(false);
    }

    @Override
    protected void initUI() {
        addLoginUI();
    }

    public void addLoginUI() {
        HBox root = new HBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(1550, 800);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f001f, #4a0060);");

        // Left: Logo
        VBox leftBox = new VBox();
        leftBox.setAlignment(Pos.CENTER);
        leftBox.setPrefWidth(481);

        Image logoImage = new Image(getClass().getResource("assets/typewiz_logo.png").toExternalForm());
        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(546);
        logo.setFitHeight(530);
        logo.setPreserveRatio(true);
        logo.setEffect(new DropShadow(6, Color.BLACK));
        leftBox.getChildren().add(logo);

        playBounceAnimation(logo);

        // Right: Login Form
        VBox rightBox = new VBox(10);
        rightBox.setAlignment(Pos.CENTER);
        rightBox.setPrefWidth(619);
        rightBox.setPrefHeight(790);

        Text loginText = FXGL.getUIFactoryService().newText("Login", Color.WHITE, 52);
        loginText.fontProperty().unbind();
        loginText.setFont(Font.font("Viner Hand ITC", 52));
        loginText.setEffect(new Glow(0.4));
        VBox.setMargin(loginText, new Insets(0, 60, 20, 0));

        HBox usernameBox = createInputField("assets/profile_icon_login.png", "Username", false);
        HBox passwordBox = createInputField("assets/password_icon.png", "Password", true);

        HBox optionsBox = new HBox(20);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox rememberMe = new CheckBox("Remember Me");
        rememberMe.setTextFill(Color.web("#a1a1a1"));
        rememberMe.setFont(Font.font("System Italic", 12));
        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        optionsBox.getChildren().addAll(rememberMe, forgotPassword);

        // Login Button
        Button loginButton = FXGL.getUIFactoryService().newButton("Login");
        loginButton.setPrefSize(378, 58);
        loginButton.fontProperty().unbind();  // Unbind the font property
        loginButton.setFont(Font.font("Book Antiqua", 20));  // Now you can safely set the font
        loginButton.setStyle("-fx-background-color: #6a0073; -fx-text-fill: white;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #9c006f; -fx-text-fill: white;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #6a0073; -fx-text-fill: white;"));
        VBox.setMargin(loginButton, new Insets(50, 80, 0, 0));

        // Sign Up Button
        Button signUpButton = FXGL.getUIFactoryService().newButton("Sign up");
        signUpButton.setPrefSize(378, 58);
        signUpButton.fontProperty().unbind();  // Unbind the font property
        signUpButton.setFont(Font.font("Book Antiqua", 20));  // Now you can safely set the font
        signUpButton.setStyle("-fx-background-color: #6a0073; -fx-text-fill: white;");
        signUpButton.setOnMouseEntered(e -> signUpButton.setStyle("-fx-background-color: #9c006f; -fx-text-fill: white;"));
        signUpButton.setOnMouseExited(e -> signUpButton.setStyle("-fx-background-color: #6a0073; -fx-text-fill: white;"));
        VBox.setMargin(signUpButton, new Insets(0, 80, 0, 0));

// Create VBox to hold the buttons and separator
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);

// Login Button
        VBox.setMargin(loginButton, new Insets(20, 0, 0, 0));

// Separator Box (Or)
        HBox separatorBox = new HBox();
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setSpacing(10);
        Separator sepLeft = new Separator();
        sepLeft.setPrefWidth(172);
        Separator sepRight = new Separator();
        sepRight.setPrefWidth(178);
        Text orText = FXGL.getUIFactoryService().newText("Or", Color.WHITE, 16);
        separatorBox.getChildren().addAll(sepLeft, orText, sepRight);

// Sign Up Button
        VBox.setMargin(signUpButton, new Insets(0, 0, 0, 0));

// Add all components to the VBox
        buttonBox.getChildren().addAll(loginButton, separatorBox, signUpButton);

// Apply slide-in animation to the VBox
        playSlideInAnimation(buttonBox, 1.2);


        rightBox.getChildren().addAll(
                animateNode(loginText, 0.2),
                animateNode(usernameBox, 0.4),
                animateNode(passwordBox, 0.6),
                animateNode(optionsBox, 0.8),
                animateNode(separatorBox, 1.0),
                buttonBox
        );

        root.getChildren().addAll(leftBox, rightBox);
        applyFadeInAnimation(root);

        FXGL.getGameScene().addUINode(root);

        // --- Placeholder for Animations and Effects ---

        // Particle Effects (Floating sparkles or bubbles around the background)
        // FXGL.getGameScene().addUINode(createParticleEmitter());

        // Background Music
        // FXGL.getAudioPlayer().loopMusic("assets/background_music.mp3");

        // Typing Sound Effect on Key Press
        // FXGL.getInput().addAction(() -> FXGL.getAudioPlayer().playSound("assets/typing_sound.wav"), KeyCode.ANY);

        // Hover Sparkle Effect on Buttons
        // addHoverSparkleEffect(loginButton);
        // addHoverSparkleEffect(signUpButton);

        // --- End of Placeholder ---
    }

    private void applyFadeInAnimation(Pane root) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void playBounceAnimation(ImageView logo) {
        logo.setTranslateY(-100);
        logo.setScaleX(0.7);
        logo.setScaleY(0.7);

        TranslateTransition translate = new TranslateTransition(Duration.seconds(1), logo);
        translate.setToY(0);
        ScaleTransition scale = new ScaleTransition(Duration.seconds(1), logo);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition bounce = new ParallelTransition(translate, scale);
        bounce.setInterpolator(Interpolator.EASE_OUT);
        bounce.play();
    }

    private void playSlideInAnimation(VBox box, double delaySeconds) {
        box.setTranslateY(100);
        box.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), box);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.seconds(delaySeconds));

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.8), box);
        slide.setFromY(100);
        slide.setToY(0);
        slide.setDelay(Duration.seconds(delaySeconds));

        fade.play();
        slide.play();
    }

    private Node animateNode(Node node, double delay) {
        node.setTranslateY(30);
        node.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.6), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.seconds(delay));

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.6), node);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setDelay(Duration.seconds(delay));

        fade.play();
        slide.play();

        return node;
    }

    private void addHoverEffect(Button button) {
        Glow glow = new Glow(0.3);
        DropShadow shadow = new DropShadow(15, Color.web("#faccff"));
        shadow.setInput(glow);

        button.setOnMouseEntered(e -> button.setEffect(shadow));
        button.setOnMouseExited(e -> button.setEffect(null));
    }

    private HBox createInputField(String iconPath, String promptText, boolean isPassword) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(54);
        box.setPrefWidth(381);
        box.setStyle("-fx-background-color: #2b1d3a; -fx-background-radius: 30;");
        box.setPadding(new Insets(0, 0, 0, 30));
        box.setEffect(new InnerShadow());

        Image iconImage = new Image(getClass().getResource(iconPath).toExternalForm());
        ImageView icon = new ImageView(iconImage);
        icon.setFitHeight(26);
        icon.setFitWidth(26);

        if (isPassword) {
            PasswordField pf = new PasswordField();
            pf.setPrefSize(253, 41);
            pf.setPromptText(promptText);
            pf.setFont(Font.font("Book Antiqua Italic", 20));
            pf.setStyle("-fx-background-color: #2b1d3a;");
            box.getChildren().addAll(icon, pf);
        } else {
            TextField tf = new TextField();
            tf.setPrefSize(279, 40);
            tf.setPromptText(promptText);
            tf.setFont(Font.font("Book Antiqua Italic", 20));
            tf.setStyle("-fx-background-color: #2b1d3a;");
            box.getChildren().addAll(icon, tf);
        }

        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
