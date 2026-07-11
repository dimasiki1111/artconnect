package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.CommunityService;
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

public class CommunityController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<CommunityMember> memberTable;
    @FXML
    private TableColumn<CommunityMember, String> nameColumn;
    @FXML
    private TableColumn<CommunityMember, String> emailColumn;
    @FXML
    private TableColumn<CommunityMember, String> cityColumn;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        var filtered = communityService.getAllMembers().stream()
                .filter(m -> query == null || query.isEmpty() ||
                        m.getName().toLowerCase().contains(query.toLowerCase()) ||
                        m.getCity().toLowerCase().contains(query.toLowerCase()))
                .toList();
        memberTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    private void handleAddMember() {
        Dialog<CommunityMember> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Membre");
        dialog.setHeaderText("Créer un nouveau membre communautaire");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField cityField = new TextField();
        cityField.setPromptText("Ville");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nom :"), nameField,
                new Label("Email :"), emailField,
                new Label("Ville :"), cityField);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                CommunityMember member = new CommunityMember();
                member.setName(nameField.getText());
                member.setEmail(emailField.getText());
                member.setCity(cityField.getText());
                return member;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(member -> {
            try {
                communityService.addMember(member);
                refreshTable();
                showInfo("Succès", "Membre créé avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de créer le membre: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditMember() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un membre à modifier");
            return;
        }

        Dialog<CommunityMember> dialog = new Dialog<>();
        dialog.setTitle("Modifier le Membre");
        dialog.setHeaderText("Modifier: " + selected.getName());

        TextField nameField = new TextField(selected.getName());
        TextField emailField = new TextField(selected.getEmail());
        TextField cityField = new TextField(selected.getCity());

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nom :"), nameField,
                new Label("Email :"), emailField,
                new Label("Ville :"), cityField);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                selected.setName(nameField.getText());
                selected.setEmail(emailField.getText());
                selected.setCity(cityField.getText());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(member -> {
            try {
                communityService.updateMember(member);
                refreshTable();
                showInfo("Succès", "Membre modifié avec succès");
            } catch (Exception e) {
                showError("Erreur", "Impossible de modifier le membre: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteMember() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un membre à supprimer");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le membre ?");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer: " + selected.getName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    communityService.deleteMember(selected.getName());
                    refreshTable();
                    showInfo("Succès", "Membre supprimé avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer le membre: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
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
