package com.project.artconnect.ui;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class GalleryController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Gallery> galleryTable;
    @FXML
    private TableColumn<Gallery, String> nameColumn;
    @FXML
    private TableColumn<Gallery, String> addressColumn;
    @FXML
    private TableColumn<Gallery, Double> ratingColumn;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
            refreshTable();
        } else {
            // Fallback to ListView if using old FXML
            initializeAsListView();
        }
    }

    private void initializeAsListView() {
        // This is kept for backward compatibility
    }

    @FXML
    private void handleSearch() {
        if (galleryTable == null)
            return;
        String query = searchField.getText();
        var filtered = galleryService.getAllGalleries().stream()
                .filter(g -> query == null || query.isEmpty() ||
                        g.getName().toLowerCase().contains(query.toLowerCase()) ||
                        g.getAddress().toLowerCase().contains(query.toLowerCase()))
                .toList();
        galleryTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    private void handleAddGallery() {
        Dialog<Gallery> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une Galerie");
        dialog.setHeaderText("Créer une nouvelle galerie");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom");
        TextField addressField = new TextField();
        addressField.setPromptText("Adresse");
        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 5.0, 3.0, 0.1);
        ratingSpinner.setPrefWidth(100);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nom :"), nameField,
                new Label("Adresse :"), addressField,
                new Label("Note (0-5) :"), ratingSpinner);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                Gallery gallery = new Gallery();
                gallery.setName(nameField.getText());
                gallery.setAddress(addressField.getText());
                gallery.setRating(ratingSpinner.getValue());
                return gallery;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(gallery -> {
            try {
                galleryService.saveGallery(gallery);
                refreshTable();
                showInfo("Succès", "Galerie créée avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de créer la galerie: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditGallery() {
        if (galleryTable == null || galleryTable.getSelectionModel().getSelectedItem() == null) {
            showError("Sélection requise", "Veuillez sélectionner une galerie à modifier");
            return;
        }

        Gallery selected = galleryTable.getSelectionModel().getSelectedItem();

        Dialog<Gallery> dialog = new Dialog<>();
        dialog.setTitle("Modifier la Galerie");
        dialog.setHeaderText("Modifier: " + selected.getName());

        TextField nameField = new TextField(selected.getName());
        TextField addressField = new TextField(selected.getAddress());
        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 5.0, selected.getRating(), 0.1);
        ratingSpinner.setPrefWidth(100);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nom :"), nameField,
                new Label("Adresse :"), addressField,
                new Label("Note (0-5) :"), ratingSpinner);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                selected.setName(nameField.getText());
                selected.setAddress(addressField.getText());
                selected.setRating(ratingSpinner.getValue());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(gallery -> {
            try {
                galleryService.updateGallery(gallery);
                refreshTable();
                showInfo("Succès", "Galerie modifiée avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de modifier la galerie: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteGallery() {
        if (galleryTable == null || galleryTable.getSelectionModel().getSelectedItem() == null) {
            showError("Sélection requise", "Veuillez sélectionner une galerie à supprimer");
            return;
        }

        Gallery selected = galleryTable.getSelectionModel().getSelectedItem();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer la galerie ?");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer: " + selected.getName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    galleryService.deleteGallery(selected.getName());
                    refreshTable();
                    showInfo("Succès", "Galerie supprimée avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer la galerie: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        if (galleryTable != null) {
            galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
        }
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
