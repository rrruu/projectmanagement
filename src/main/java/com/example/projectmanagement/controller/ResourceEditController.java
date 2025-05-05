package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.ResourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ResourceEditController {


    @FXML
    private TextField nameField;
    @FXML private TextField idField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField rateField;

    private ResourceModel resourceToEdit = null;
    private boolean isConfirmed = false;

    public void setResourceToEdit(ResourceModel resource){
        this.resourceToEdit = resource;

        //将任务数据填充到表单
        nameField.setText(resource.getName());
        idField.setText(resource.getId());
        phoneField.setText(resource.getPhone());
        emailField.setText(resource.getEmail());
        typeCombo.setValue(resource.getType());
        rateField.setText(String.valueOf(resource.getDailyRate()));
    }




    private void validateAndUpdateResource(){
        //验证必填字段
        validateRequiredFields();

        //验证价格有效性
        validateRateRange();

        double rate = Double.parseDouble(rateField.getText().trim());



        //更新任务属性
        resourceToEdit.setName(nameField.getText().trim());
        resourceToEdit.setId(idField.getText().trim());
        resourceToEdit.setPhone(phoneField.getText().trim());
        resourceToEdit.setEmail(emailField.getText().trim());
        resourceToEdit.setType(typeCombo.getValue().trim());
        resourceToEdit.setDailyRate(rate);

    }



    private void validateRequiredFields(){
        if(idField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("资源ID不能为空");
        }
    }

    private void validateRateRange(){
        double rate = Double.parseDouble(rateField.getText().trim());
        if(rate < 0){
            throw new IllegalArgumentException("单价必须大于0");
        }
    }

    @FXML
    private void handleConfirm(){
        try {
            validateAndUpdateResource();
            isConfirmed = true;
            idField.getScene().getWindow().hide();
        } catch (IllegalArgumentException e) {
            //显示错误提示（保持在当前窗口）
            new Alert(Alert.AlertType.ERROR,e.getMessage(), ButtonType.OK).show();
        }
    }



    @FXML
    private void handleCancel() {
        isConfirmed = false;
        idField.getScene().getWindow().hide();
    }


    public boolean isConfirmed() {
        return isConfirmed;
    }




}
