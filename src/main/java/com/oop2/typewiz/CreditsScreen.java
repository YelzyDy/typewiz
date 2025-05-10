package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2a0845, #4a148c);");

        Rectangle panel = new Rectangle(500, 500);
        panel.setArcHeight(30);
        panel.setArcWidth(30);
        panel.setFill(Color.web("rgba(60, 0, 90, 0.5)"));
        panel.setStroke(Color.web("#b388ff"));
        panel.setStrokeWidth(2);
        panel.setEffect(new DropShadow(20, Color.web("#ffeb3b", 0.3)));

        Text title = new Text("Council of Creators");
        title.setFont(Font.font("Papyrus", 36));
        title.setFill(Color.web("#ffeb3b"));
        title.setEffect(new Glow(0.5));

        VBox namesBox = new VBox(15,
                createName("• CABISO, Chestine May Mari C."),
                createName("• DAYONOT, Axille"),
                createName("• ESPINA, Ruhmer Jairus R."),
                createName("• ONG, Lovely Shane P.")
        );
        namesBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back to Tower");
        backButton.setFont(Font.font("Consolas", 20));
        backButton.setTextFill(Color.web("#e2b0ff"));
        backButton.setBackground(Background.EMPTY);
        backButton.setBorder(new Border(new BorderStroke(
                Color.web("#b388ff"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));
        backButton.setOnAction(e -> backAction.run());

        VBox layout = new VBox(30, title, namesBox, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        StackPane glassPane = new StackPane(panel, layout);
        root.getChildren().add(glassPane);
        getContentRoot().getChildren().add(root);
    }

    private Text createName(String name) {
        Text nameText = new Text(name);
        nameText.setFont(Font.font("Consolas", 22));
        nameText.setFill(Color.web("#d1c4e9"));
        return nameText;
    }
}
