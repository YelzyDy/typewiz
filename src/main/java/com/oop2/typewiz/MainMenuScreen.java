package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.FollowComponent;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.text.Font;

public class MainMenuScreen extends FXGLMenu {

    public MainMenuScreen() {
        super(MenuType.MAIN_MENU);

        Pane root = new AnchorPane();
        root.setPrefSize(1550, 800);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f001f, #4a0060);");

        VBox vbox = new VBox(40);
        vbox.setLayoutX(557);
        vbox.setLayoutY(118);

        Button startButton = createImageButton("assets/start.png", "Start", event -> {
            System.out.println("Start clicked");
            FXGL.getGameController().gotoMainMenu();
            FXGL.getGameController().startNewGame();  // Or switch to difficulty screen
        });

        Button helpButton = createImageButton("assets/help.png", "Help", event -> {
            System.out.println("Help clicked");
            // Optional: Display help screen
        });

        Button exitButton = createImageButton("assets/exit.png", "Exit", event -> {
            System.out.println("Exit clicked");
            FXGL.getGameController().exit();
        });

        vbox.getChildren().addAll(startButton, helpButton, exitButton);
        root.getChildren().add(vbox);

        getContentRoot().getChildren().add(root);
    }

    private Button createImageButton(String imagePath, String altText, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button();
        button.setPrefSize(443, 100);
        button.setStyle("-fx-background-radius: 50; -fx-background-color: transparent;");
        button.setFont(Font.font("Book Antiqua", 46));
        button.setTextFill(Color.WHITE);
        button.setEffect(new InnerShadow());
        button.setOnAction(handler);

        try {
            Image image = new Image(getClass().getResource(imagePath).toExternalForm());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(443);
            imageView.setFitHeight(100);
            imageView.setEffect(new DropShadow(10, Color.rgb(50, 18, 80)));
            button.setGraphic(imageView);
        } catch (NullPointerException e) {
            System.err.println("Image not found: " + imagePath + " (displaying fallback text)");
            button.setText(altText);
        }

        return button;
    }

}
