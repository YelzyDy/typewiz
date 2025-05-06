package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class RegisterScreen extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1550);
        settings.setHeight(800);
        settings.setTitle("TypeWiz Registration");
    }

    @Override
    protected void initUI() {
        HBox root = new HBox(10);
        root.setPrefSize(1550, 800);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f001f, #4a0060);");

        // Left VBox with Logo
        VBox leftPane = new VBox();
        leftPane.setPrefSize(481, 800);
        leftPane.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView(new Image(
                getClass().getResource("assets/typewiz_logo.png").toExternalForm()));
        logo.setFitHeight(530);
        logo.setFitWidth(546);
        logo.setPreserveRatio(true);
        logo.setEffect(new DropShadow(20, Color.color(0.16, 0.14, 0.14)));

        leftPane.getChildren().add(logo);

        // Right VBox with Form
        VBox formPane = new VBox(10);
        formPane.setPrefSize(619, 725);
        formPane.setAlignment(Pos.CENTER);

        Label title = new Label("Sign up");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Viner Hand ITC", 52));
        VBox.setMargin(title, new Insets(0, 60, 20, 0));

        TextField tfUsername = createInputField("Username", "assets/profile_icon_login.png");
        TextField tfEmail = createInputField("Email", "assets/profile_icon_login.png");
        PasswordField tfPassword = createPasswordField("Password", "assets/password_icon.png");
        PasswordField tfConfirmPassword = createPasswordField("Confirm Password", "assets/password_icon.png");

        Button createAccountBtn = new Button("Create Account");
        styleButton(createAccountBtn, "#c85bff", Color.WHITE);
        createAccountBtn.setOnAction(e -> System.out.println("Create Account clicked"));

        HBox separatorBox = new HBox();
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setSpacing(10);
        Separator sepLeft = new Separator();
        sepLeft.setPrefWidth(172);
        Separator sepRight = new Separator();
        sepRight.setPrefWidth(178);
        Text orText = FXGL.getUIFactoryService().newText("Or", Color.WHITE, 16);
        separatorBox.getChildren().addAll(sepLeft, orText, sepRight);

        Button loginBtn = new Button("Login");
        styleButton(loginBtn, "#ffffff", Color.BLACK);
        loginBtn.setOnAction(e -> System.out.println("Login clicked"));

        formPane.getChildren().addAll(title, tfUsername, tfEmail, tfPassword, tfConfirmPassword, createAccountBtn, separatorBox, loginBtn);

        root.getChildren().addAll(leftPane, formPane);

        FXGL.getGameScene().addUINode(root);
    }

    private TextField createInputField(String prompt, String iconPath) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #2b1d3a; -fx-background-radius: 30;");
        box.setPrefSize(381, 54);
        box.setEffect(new InnerShadow());

        ImageView icon = new ImageView(new Image(
                getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(26);
        icon.setFitWidth(26);

        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Book Antiqua Italic", 20));
        tf.setPrefSize(279, 40);
        tf.setStyle("-fx-background-color: #2b1d3a; -fx-text-fill: white;");

        box.getChildren().addAll(icon, tf);
        VBox wrapper = new VBox(box);
        return tf;
    }

    private PasswordField createPasswordField(String prompt, String iconPath) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #2b1d3a; -fx-background-radius: 30;");
        box.setPrefSize(381, 54);
        box.setEffect(new InnerShadow());

        ImageView icon = new ImageView(new Image(
                getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(26);
        icon.setFitWidth(26);

        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setFont(Font.font("Book Antiqua Italic", 20));
        pf.setPrefSize(253, 41);
        pf.setStyle("-fx-background-color: #2b1d3a; -fx-text-fill: white;");

        box.getChildren().addAll(icon, pf);
        VBox wrapper = new VBox(box);
        return pf;
    }

    private void styleButton(Button button, String bgColor, Color textColor) {
        button.setPrefSize(378, 58);
        button.setFont(Font.font("Book Antiqua", 20));
        button.setTextFill(textColor);
        button.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 30;");
        button.setEffect(new InnerShadow());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
