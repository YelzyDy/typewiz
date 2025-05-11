package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;

public class CreditsScreen extends FXGLMenu {

    public Pane getAsPane() {
        return (Pane) getContentRoot();
    }

    public static Node create(Runnable backAction) {
        return new CreditsScreen(backAction).getContentRoot();
    }

    CreditsScreen(Runnable backAction) {
        super(MenuType.GAME_MENU);

        StackPane root = new StackPane();
        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        root.setStyle(
                "-fx-background-image: url('assets/textures/background-and-platforms/creditsbg.png');" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        Pane starField = createMagicalStars();
        Text title = new Text("Council of the Enchanted");
        title.setFont(Font.font("Papyrus", 42));
        title.setFill(Color.web("#ffee58"));
        title.setEffect(new Glow(0.8));
        applyWobble(title);

        VBox namesSection = new VBox(20,
                createSigil("CABISO, Chestine May Mari C."),
                createSigil("DAYONOT, Axille"),
                createSigil("ESPINA, Ruhmer Jairus R."),
                createSigil("ONG, Lovely Shane P.")
        );
        namesSection.setAlignment(Pos.CENTER);

//        Button backButton = new Button("Return to the Tower");
//        backButton.setFont(Font.font("Consolas", 20));
//        backButton.setTextFill(Color.web("#ce93d8"));
//        backButton.setBackground(Background.EMPTY);
//        backButton.setBorder(new Border(new BorderStroke(
//                Color.web("#ba68c8"),
//                BorderStrokeStyle.SOLID,
//                new CornerRadii(8),
//                new BorderWidths(2)
//        )));

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
            // Transition back to the main menu
            FXGL.getGameScene().removeUINode(getContentRoot());
            FXGL.getGameScene().getContentRoot().getChildren().add(new MainMenuScreen().getContentRoot());
        });

        VBox layout = new VBox(50, title, namesSection, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        root.getChildren().addAll(starField, layout);
        getContentRoot().getChildren().add(root);
    }

    private StackPane createSigil(String name) {
        Rectangle sigil = new Rectangle(400, 70);
        sigil.setArcWidth(25);
        sigil.setArcHeight(25);
        sigil.setFill(Color.web("rgba(180, 136, 255, 0.15)"));
        sigil.setStroke(Color.web("#d1c4e9"));
        sigil.setStrokeWidth(2);
        sigil.setEffect(new DropShadow(15, Color.web("#ffffff40")));

        Text nameText = new Text(name);
        nameText.setFont(Font.font("Consolas", 20));
        nameText.setFill(Color.web("#f3e5f5"));
        nameText.setEffect(new Glow(0.3));

        StackPane sigilContainer = new StackPane(sigil, nameText);
        sigilContainer.setAlignment(Pos.CENTER);
        applyFloatAnimation(sigilContainer);
        return sigilContainer;
    }

    private void applyWobble(Text text) {
        RotateTransition wobble = new RotateTransition(Duration.seconds(3), text);
        wobble.setByAngle(5);
        wobble.setAutoReverse(true);
        wobble.setCycleCount(Animation.INDEFINITE);
        wobble.play();
    }

    private void applyFloatAnimation(Node node) {
        TranslateTransition floatUpDown = new TranslateTransition(Duration.seconds(2 + Math.random() * 2), node);
        floatUpDown.setByY(10);
        floatUpDown.setAutoReverse(true);
        floatUpDown.setCycleCount(Animation.INDEFINITE);
        floatUpDown.play();
    }

    private void addMagicHover(Button button) {
        button.setOnMouseEntered(e -> {
            button.setTextFill(Color.web("#f8bbd0"));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        button.setOnMouseExited(e -> {
            button.setTextFill(Color.web("#ce93d8"));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private Pane createMagicalStars() {
        Pane starPane = new Pane();
        starPane.setPickOnBounds(false);

        Random rand = new Random();
        for (int i = 0; i < 40; i++) {
            Circle star = new Circle(2, Color.web("#ffffffa0"));
            star.setTranslateX(rand.nextDouble() * FXGL.getAppWidth());
            star.setTranslateY(rand.nextDouble() * FXGL.getAppHeight());
            starPane.getChildren().add(star);

            TranslateTransition drift = new TranslateTransition(Duration.seconds(6 + rand.nextDouble() * 5), star);
            drift.setByY(-30 + rand.nextDouble() * 60);
            drift.setByX(-10 + rand.nextDouble() * 20);
            drift.setAutoReverse(true);
            drift.setCycleCount(Animation.INDEFINITE);
            drift.play();

            FadeTransition flicker = new FadeTransition(Duration.seconds(2 + rand.nextDouble() * 2), star);
            flicker.setFromValue(1.0);
            flicker.setToValue(0.2);
            flicker.setAutoReverse(true);
            flicker.setCycleCount(Animation.INDEFINITE);
            flicker.play();
        }

        return starPane;
    }
}
