package com.project.artconnect.ui;

import java.time.LocalDate;
import java.util.List;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ExhibitionController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Exhibition> exhibitionTable;
    @FXML
    private TableColumn<Exhibition, String> titleColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> dateColumn;
    @FXML
    private TableColumn<Exhibition, String> themeColumn;
    @FXML
    private TableColumn<Exhibition, String> galleryColumn;

    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();
    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));

        galleryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGallery() != null ? cellData.getValue().getGallery().getName() : "Unknown"));

        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        var filtered = exhibitionService.getAllExhibitions().stream()
                .filter(e -> query == null || query.isEmpty() ||
                        e.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        e.getTheme().toLowerCase().contains(query.toLowerCase()))
                .toList();
        exhibitionTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    private void handleAddExhibition() {
        Dialog<Exhibition> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une Exposition");
        dialog.setHeaderText("Créer une nouvelle exposition");

        TextField titleField = new TextField();
        titleField.setPromptText("Titre");
        TextField themeField = new TextField();
        themeField.setPromptText("Thème");
        DatePicker dateField = new DatePicker(LocalDate.now());
        ComboBox<Gallery> galleryCombo = new ComboBox<>();
        galleryCombo.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Titre :"), titleField,
                new Label("Thème :"), themeField,
                new Label("Date :"), dateField,
                new Label("Galerie :"), galleryCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                Exhibition exhibition = new Exhibition();
                exhibition.setTitle(titleField.getText());
                exhibition.setTheme(themeField.getText());
                exhibition.setStartDate(dateField.getValue());
                exhibition.setGallery(galleryCombo.getValue());
                return exhibition;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(exhibition -> {
            try {
                exhibitionService.saveExhibition(exhibition);
                refreshTable();
                showInfo("Succès", "Exposition créée avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de créer l'exposition: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditExhibition() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner une exposition à modifier");
            return;
        }

        Dialog<Exhibition> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'Exposition");
        dialog.setHeaderText("Modifier: " + selected.getTitle());

        TextField titleField = new TextField(selected.getTitle());
        TextField themeField = new TextField(selected.getTheme());
        DatePicker dateField = new DatePicker(selected.getStartDate());
        ComboBox<Gallery> galleryCombo = new ComboBox<>();
        galleryCombo.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
        galleryCombo.setValue(selected.getGallery());

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Titre :"), titleField,
                new Label("Thème :"), themeField,
                new Label("Date :"), dateField,
                new Label("Galerie :"), galleryCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                selected.setTitle(titleField.getText());
                selected.setTheme(themeField.getText());
                selected.setStartDate(dateField.getValue());
                selected.setGallery(galleryCombo.getValue());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(exhibition -> {
            try {
                exhibitionService.updateExhibition(exhibition);
                refreshTable();
                showInfo("Succès", "Exposition modifiée avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de modifier l'exposition: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteExhibition() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner une exposition à supprimer");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer l'exposition ?");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer: " + selected.getTitle() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    exhibitionService.deleteExhibition(selected.getTitle());
                    refreshTable();
                    showInfo("Succès", "Exposition supprimée avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer l'exposition: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        List<Exhibition> exhibitions = exhibitionService.getAllExhibitions();
        exhibitionTable.setItems(FXCollections.observableArrayList(exhibitions));
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
