package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        artistTable.setItems(FXCollections.observableArrayList(artistService.searchArtists(query, null, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    private void handleAddArtist() {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Artiste");
        dialog.setHeaderText("Créer un nouvel artiste");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom");
        TextField cityField = new TextField();
        cityField.setPromptText("Ville");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField yearField = new TextField();
        yearField.setPromptText("Année de naissance");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nom :"), nameField,
                new Label("Ville :"), cityField,
                new Label("Email :"), emailField,
                new Label("Année de naissance :"), yearField);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                try {
                    Artist artist = new Artist();
                    artist.setName(nameField.getText());
                    artist.setCity(cityField.getText());
                    artist.setContactEmail(emailField.getText());
                    artist.setBirthYear(Integer.parseInt(yearField.getText()));
                    return artist;
                } catch (NumberFormatException e) {
                    showError("Erreur d'entrée", "L'année de naissance doit être un nombre valide");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(artist -> {
            try {
                artistService.createArtist(artist);
                refreshTable();
                showInfo("Succès", "Artiste créé avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de créer l'artiste: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un artiste à modifier");
            return;
        }

        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'Artiste");
        dialog.setHeaderText("Modifier: " + selected.getName());

        TextField nameField = new TextField(selected.getName());
        TextField cityField = new TextField(selected.getCity());
        TextField emailField = new TextField(selected.getContactEmail());
        TextField yearField = new TextField(String.valueOf(selected.getBirthYear()));

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nom :"), nameField,
                new Label("Ville :"), cityField,
                new Label("Email :"), emailField,
                new Label("Année de naissance :"), yearField);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                try {
                    selected.setName(nameField.getText());
                    selected.setCity(cityField.getText());
                    selected.setContactEmail(emailField.getText());
                    selected.setBirthYear(Integer.parseInt(yearField.getText()));
                    return selected;
                } catch (NumberFormatException e) {
                    showError("Erreur d'entrée", "L'année de naissance doit être un nombre valide");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(artist -> {
            try {
                artistService.updateArtist(artist);
                refreshTable();
                showInfo("Succès", "Artiste modifié avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de modifier l'artiste: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un artiste à supprimer");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer l'artiste ?");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer l'artiste: " + selected.getName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    artistService.deleteArtist(selected.getName());
                    refreshTable();
                    showInfo("Succès", "Artiste supprimé avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer l'artiste: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
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
