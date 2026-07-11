package com.project.artconnect.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.WorkshopService;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WorkshopController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Workshop> workshopTable;
    @FXML
    private TableColumn<Workshop, String> titleColumn;
    @FXML
    private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<Workshop, String> instructorColumn;
    @FXML
    private TableColumn<Workshop, Double> priceColumn;
    @FXML
    private TableColumn<Workshop, String> levelColumn;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));

        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null ? cellData.getValue().getInstructor().getName()
                        : "Unknown"));

        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        var filtered = workshopService.getAllWorkshops().stream()
                .filter(w -> query == null || query.isEmpty() ||
                        w.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        (w.getInstructor() != null
                                && w.getInstructor().getName().toLowerCase().contains(query.toLowerCase())))
                .toList();
        workshopTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    private void handleAddWorkshop() {
        Dialog<Workshop> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Atelier");
        dialog.setHeaderText("Créer un nouvel atelier");

        TextField titleField = new TextField();
        titleField.setPromptText("Titre");
        DatePicker dateField = new DatePicker();
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 14);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        ComboBox<Artist> instructorCombo = new ComboBox<>();
        instructorCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        instructorCombo.setPromptText("Sélectionner l'instructeur");
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 1000.0, 50.0, 5.0);
        priceSpinner.setPrefWidth(100);
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.setItems(FXCollections.observableArrayList("Débutant", "Intermédiaire", "Avancé"));
        levelCombo.setValue("Débutant");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Titre :"), titleField,
                new Label("Date :"), dateField,
                new Label("Heure (HH:mm):"),
                new HBox(5, new Spinner<>(0, 23, 14), new Spinner<>(0, 59, 0)),
                new Label("Instructeur :"), instructorCombo,
                new Label("Prix :"), priceSpinner,
                new Label("Niveau :"), levelCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton && dateField.getValue() != null && instructorCombo.getValue() != null) {
                Workshop workshop = new Workshop();
                workshop.setTitle(titleField.getText());
                LocalDate date = dateField.getValue();
                workshop.setDate(LocalDateTime.of(date,
                        java.time.LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())));
                workshop.setInstructor(instructorCombo.getValue());
                workshop.setPrice(priceSpinner.getValue());
                workshop.setLevel(levelCombo.getValue());
                return workshop;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(workshop -> {
            try {
                workshopService.saveWorkshop(workshop);
                refreshTable();
                showInfo("Succès", "Atelier créé avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de créer l'atelier: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditWorkshop() {
        if (workshopTable.getSelectionModel().getSelectedItem() == null) {
            showError("Sélection requise", "Veuillez sélectionner un atelier à modifier");
            return;
        }

        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();

        Dialog<Workshop> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'Atelier");
        dialog.setHeaderText("Modifier: " + selected.getTitle());

        TextField titleField = new TextField(selected.getTitle());
        DatePicker dateField = new DatePicker(selected.getDate().toLocalDate());
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, selected.getDate().getHour());
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, selected.getDate().getMinute());
        ComboBox<Artist> instructorCombo = new ComboBox<>();
        instructorCombo.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        instructorCombo.setValue(selected.getInstructor());
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 1000.0, selected.getPrice(), 5.0);
        priceSpinner.setPrefWidth(100);
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.setItems(FXCollections.observableArrayList("Débutant", "Intermédiaire", "Avancé"));
        levelCombo.setValue(selected.getLevel());

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Titre :"), titleField,
                new Label("Date :"), dateField,
                new Label("Heure (HH:mm):"),
                new HBox(5, hourSpinner, minuteSpinner),
                new Label("Instructeur :"), instructorCombo,
                new Label("Prix :"), priceSpinner,
                new Label("Niveau :"), levelCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                selected.setTitle(titleField.getText());
                LocalDate date = dateField.getValue();
                selected.setDate(LocalDateTime.of(date,
                        java.time.LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())));
                selected.setInstructor(instructorCombo.getValue());
                selected.setPrice(priceSpinner.getValue());
                selected.setLevel(levelCombo.getValue());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(workshop -> {
            try {
                workshopService.updateWorkshop(workshop);
                refreshTable();
                showInfo("Succès", "Atelier modifié avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de modifier l'atelier: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteWorkshop() {
        if (workshopTable.getSelectionModel().getSelectedItem() == null) {
            showError("Sélection requise", "Veuillez sélectionner un atelier à supprimer");
            return;
        }

        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer l'atelier ?");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer: " + selected.getTitle() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    workshopService.deleteWorkshop(selected.getTitle().hashCode());
                    refreshTable();
                    showInfo("Succès", "Atelier supprimé avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer l'atelier: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
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
