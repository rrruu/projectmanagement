package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.ResourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ResourceEditController {


    @FXML
    public TextField nameField;
    @FXML public TextField idField;
    @FXML public TextField phoneField;
    @FXML public TextField emailField;
    @FXML public ComboBox<String> typeCombo;
    @FXML public TextField rateField;
    @FXML public TextArea commentField;

    private ResourceModel resourceToEdit = null;
    private boolean isConfirmed = false;


    @FXML
    private void initialize() {
        typeCombo.getItems().addAll("人力", "设备", "场地");
    }




    public void setResourceToEdit(ResourceModel resource){
        this.resourceToEdit = resource;

        //将任务数据填充到表单
        nameField.setText(resource.getName());
        idField.setText(resource.getId());
        phoneField.setText(resource.getPhone());
        emailField.setText(resource.getEmail());
        typeCombo.setValue(resource.getType());
        rateField.setText(String.valueOf(resource.getDailyRate()));
        commentField.setText(resource.getComment());
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
        resourceToEdit.setComment(commentField.getText().trim());

    }



    private void validateRequiredFields(){
        if(idField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("资源ID不能为空");
        }
        if(typeCombo.getValue() == null){
            throw new IllegalArgumentException("必须选择资源类型");
        }
        if(rateField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("资源单价不能为空");
        }

    }

    private void validateRateRange(){
        try {
            double rate = Double.parseDouble(rateField.getText().trim());
            if(rate < 0){
                throw new IllegalArgumentException("单价必须大于0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("单价必须为有效数字");
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
