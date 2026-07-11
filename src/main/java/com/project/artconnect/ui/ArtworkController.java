package com.project.artconnect.ui;

import java.util.HashSet;
import java.util.Set;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ArtworkController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> typeFilter;
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> titleColumn;
    @FXML
    private TableColumn<Artwork, String> typeColumn;
    @FXML
    private TableColumn<Artwork, Double> priceColumn;
    @FXML
    private TableColumn<Artwork, String> statusColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        // Initialize type filter with available types
        initializeTypeFilter();
        refreshTable();
    }

    private void initializeTypeFilter() {
        Set<String> types = new HashSet<>();
        artworkService.getAllArtworks().stream()
                .map(Artwork::getType)
                .filter(type -> type != null && !type.isEmpty())
                .forEach(types::add);

        typeFilter.setItems(FXCollections.observableArrayList(types));
        typeFilter.setOnAction(event -> handleFilter());
    }

    @FXML
    private void handleSearch() {
        handleFilter();
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        typeFilter.setValue(null);
        refreshTable();
    }

    private void handleFilter() {
        String searchQuery = searchField.getText();
        String selectedType = typeFilter.getValue();

        var filtered = artworkService.getAllArtworks().stream()
                .filter(artwork -> searchQuery == null || searchQuery.isEmpty() ||
                        artwork.getTitle().toLowerCase().contains(searchQuery.toLowerCase()))
                .filter(artwork -> selectedType == null || selectedType.isEmpty() ||
                        artwork.getType().equalsIgnoreCase(selectedType))
                .toList();

        artworkTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    @FXML
    private void handleAddArtwork() {
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une Oeuvre");
        dialog.setHeaderText("Créer une nouvelle oeuvre");

        TextField titleField = new TextField();
        titleField.setPromptText("Titre");
        TextField typeField = new TextField();
        typeField.setPromptText("Type (Discipline)");
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 100000.0, 1000.0, 100.0);
        priceSpinner.setPrefWidth(100);
        ComboBox<Artwork.Status> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(Artwork.Status.values()));
        statusCombo.setValue(Artwork.Status.FOR_SALE);
        ComboBox<Artist> artistCombo = new ComboBox<>();
        artistCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        artistCombo.setPromptText("Sélectionner l'artiste");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Titre :"), titleField,
                new Label("Type :"), typeField,
                new Label("Prix :"), priceSpinner,
                new Label("Statut :"), statusCombo,
                new Label("Artiste :"), artistCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton && artistCombo.getValue() != null) {
                Artwork artwork = new Artwork();
                artwork.setTitle(titleField.getText());
                artwork.setType(typeField.getText());
                artwork.setPrice(priceSpinner.getValue());
                artwork.setStatus(statusCombo.getValue());
                artwork.setArtist(artistCombo.getValue());
                return artwork;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(artwork -> {
            try {
                artworkService.createArtwork(artwork);
                refreshTable();
                initializeTypeFilter();
                showInfo("Succès", "Oeuvre créée avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de créer l'oeuvre: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditArtwork() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner une oeuvre à modifier");
            return;
        }

        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'Oeuvre");
        dialog.setHeaderText("Modifier: " + selected.getTitle());

        TextField titleField = new TextField(selected.getTitle());
        TextField typeField = new TextField(selected.getType());
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 100000.0, selected.getPrice(), 100.0);
        priceSpinner.setPrefWidth(100);
        ComboBox<Artwork.Status> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(Artwork.Status.values()));
        statusCombo.setValue(selected.getStatus());
        ComboBox<Artist> artistCombo = new ComboBox<>();
        artistCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        artistCombo.setValue(selected.getArtist());

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Titre :"), titleField,
                new Label("Type :"), typeField,
                new Label("Prix :"), priceSpinner,
                new Label("Statut :"), statusCombo,
                new Label("Artiste :"), artistCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                selected.setTitle(titleField.getText());
                selected.setType(typeField.getText());
                selected.setPrice(priceSpinner.getValue());
                selected.setStatus(statusCombo.getValue());
                selected.setArtist(artistCombo.getValue());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(artwork -> {
            try {
                artworkService.updateArtwork(artwork);
                refreshTable();
                initializeTypeFilter();
                showInfo("Succès", "Oeuvre modifiée avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de modifier l'oeuvre: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteArtwork() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner une oeuvre à supprimer");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer l'oeuvre ?");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer: " + selected.getTitle() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    artworkService.deleteArtwork(selected.getTitle());
                    refreshTable();
                    initializeTypeFilter();
                    showInfo("Succès", "Oeuvre supprimée avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer l'oeuvre: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
