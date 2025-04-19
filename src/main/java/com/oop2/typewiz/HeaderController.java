package com.oop2.typewiz;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;

public class HeaderController {

    @FXML private ImageView logoIcon;
    @FXML private ImageView logoText;
    @FXML private ImageView leaderboardIcon;
    @FXML private ImageView aboutIcon;
    @FXML private ImageView profileIcon;
    @FXML private ImageView settingsIcon;

    @FXML
    public void initialize() {
        // Load images
        logoIcon.setImage(new Image(getClass().getResource("assets/typewiz_icon.png").toExternalForm()));
        logoText.setImage(new Image(getClass().getResource("assets/typewiz_text.png").toExternalForm()));
        leaderboardIcon.setImage(new Image(getClass().getResource("assets/leaderboard.png").toExternalForm()));
        aboutIcon.setImage(new Image(getClass().getResource("assets/about.png").toExternalForm()));
        profileIcon.setImage(new Image(getClass().getResource("assets/profile.png").toExternalForm()));
        settingsIcon.setImage(new Image(getClass().getResource("assets/settings.png").toExternalForm()));

        // Tooltips
        Tooltip.install(leaderboardIcon, new Tooltip("Leaderboard"));
        Tooltip.install(aboutIcon, new Tooltip("About Us"));
        Tooltip.install(profileIcon, new Tooltip("Profile"));
        Tooltip.install(settingsIcon, new Tooltip("Settings"));

        // Add click events
        leaderboardIcon.setOnMouseClicked(e -> System.out.println("Leaderboard clicked"));
        aboutIcon.setOnMouseClicked(e -> System.out.println("About clicked"));
        profileIcon.setOnMouseClicked(e -> System.out.println("Profile clicked"));
        settingsIcon.setOnMouseClicked(e -> System.out.println("Settings clicked"));


    }
}
