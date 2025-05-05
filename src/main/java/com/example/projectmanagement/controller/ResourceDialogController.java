package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.ResourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ResourceDialogController {


    @FXML
    private TextField nameField;
    @FXML private TextField idField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField rateField;

    private ResourceModel resource;

    private boolean confirmed = false;


    @FXML
    private void initialize() {
        typeCombo.getItems().addAll("人力", "设备", "场地");
    }

    public void setResource(ResourceModel resource) {
        this.resource = resource;
        nameField.setText(resource.getName());
        idField.setText(resource.getId());
        phoneField.setText(resource.getPhone());
        emailField.setText(resource.getEmail());
        typeCombo.setValue(resource.getType());
        rateField.setText(String.valueOf(resource.getDailyRate()));
    }

    public ResourceModel getResource() {
        if (resource == null) {
            resource = new ResourceModel();
        }
        resource.setName(nameField.getText());
        resource.setId(idField.getText());
        resource.setPhone(phoneField.getText());
        resource.setEmail(emailField.getText());
        resource.setType(typeCombo.getValue());
        try {
            resource.setDailyRate(Double.parseDouble(rateField.getText()));
        } catch (NumberFormatException e) {
            resource.setDailyRate(0.0);
        }
        return resource;
    }



    @FXML
    private void handleConfirm() {
        confirmed = true;
        nameField.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        nameField.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
