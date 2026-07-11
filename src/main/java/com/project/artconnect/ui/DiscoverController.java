package com.project.artconnect.ui;

import java.util.List;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class DiscoverController {
    @FXML
    private FlowPane discoverPane;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();
    private final ArtworkService artworkService = ServiceProvider.getArtworkService();

    @FXML
    public void initialize() {
        // Add 3 Featured Exhibitions (Blue theme)
        List<Exhibition> allExhibitions = exhibitionService.getAllExhibitions();
        allExhibitions.stream().limit(3).forEach(this::addExhibitionCard);

        // Add 3 Artworks (Green theme)
        List<Artwork> allArtworks = artworkService.getAllArtworks();
        allArtworks.stream().limit(3).forEach(this::addArtworkCard);

        // Add 3 Workshops (Orange/Brown theme)
        workshopService.getAllWorkshops().stream().limit(3).forEach(this::addWorkshopCard);
    }

    private void addExhibitionCard(Exhibition e) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #E3F2FD; " +
                        "-fx-border-color: #2196F3; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");
        card.setPrefWidth(280);
        card.setPrefHeight(150);

        Label titleLabel = new Label("📋 FEATURED EXHIBITION");
        titleLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #1976D2;");

        Label nameLabel = new Label(e.getTitle() != null ? e.getTitle() : "Unknown Exhibition");
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        nameLabel.setWrapText(true);

        Label themeLabel = new Label("Theme: " + (e.getTheme() != null ? e.getTheme() : "N/A"));
        themeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #424242;");

        Label galleryLabel = new Label("Gallery: " + (e.getGallery() != null ? e.getGallery().getName() : "Unknown"));
        galleryLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #616161;");
        galleryLabel.setWrapText(true);

        Label dateLabel = new Label("Date: " + (e.getStartDate() != null ? e.getStartDate() : "TBD"));
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #757575;");

        card.getChildren().addAll(titleLabel, nameLabel, themeLabel, galleryLabel, dateLabel);
        discoverPane.getChildren().add(card);
    }

    private void addArtworkCard(Artwork a) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #E8F5E9; " +
                        "-fx-border-color: #4CAF50; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");
        card.setPrefWidth(280);
        card.setPrefHeight(150);

        Label titleLabel = new Label("🎨 FEATURED ARTWORK");
        titleLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #2E7D32;");

        Label nameLabel = new Label(a.getTitle() != null ? a.getTitle() : "Unknown Artwork");
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");
        nameLabel.setWrapText(true);

        Label artistLabel = new Label("Artist: " + (a.getArtist() != null ? a.getArtist().getName() : "Unknown"));
        artistLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #424242;");
        artistLabel.setWrapText(true);

        Label typeLabel = new Label("Type: " + (a.getType() != null ? a.getType() : "N/A"));
        typeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #616161;");

        Label priceLabel = new Label("Price: $" + a.getPrice());
        priceLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, nameLabel, artistLabel, typeLabel, priceLabel);
        discoverPane.getChildren().add(card);
    }

    private void addWorkshopCard(Workshop w) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #FFF3E0; " +
                        "-fx-border-color: #FF9800; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");
        card.setPrefWidth(280);
        card.setPrefHeight(150);

        Label titleLabel = new Label("🎓 UPCOMING WORKSHOP");
        titleLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #E65100;");

        Label nameLabel = new Label(w.getTitle() != null ? w.getTitle() : "Unknown Workshop");
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #BF360C;");
        nameLabel.setWrapText(true);

        Label instructorLabel = new Label(
                "Instructor: " + (w.getInstructor() != null ? w.getInstructor().getName() : "Unknown"));
        instructorLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #424242;");
        instructorLabel.setWrapText(true);

        Label levelLabel = new Label("Level: " + (w.getLevel() != null ? w.getLevel() : "N/A"));
        levelLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #616161;");

        Label priceLabel = new Label("Price: $" + w.getPrice());
        priceLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #E65100; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, nameLabel, instructorLabel, levelLabel, priceLabel);
        discoverPane.getChildren().add(card);
    }
}
