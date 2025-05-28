package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.ResourceDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class ResourceAddController {


    @FXML
    public TextField nameField;
    @FXML public TextField idField;
    @FXML public TextField phoneField;
    @FXML public TextField emailField;
    @FXML public ComboBox<String> typeCombo;
    @FXML public TextField rateField;
    @FXML public TextArea commentField;

    private ResourceModel newResource = null;

    public ResourceModel getNewResource(){
        return newResource;
    }

//    private boolean confirmed = false;


    @FXML
    private void initialize() {
        typeCombo.getItems().addAll("人力", "设备", "场地");
    }

    @FXML
    private void handleConfirm() {
        DatabaseManager.executeTransaction(() -> {
            try {
                newResource = validateAndCreateResource();

                // 使用DAO创建资源
                ResourceDAO.create(newResource);

                // 增量添加到数据模型
                DataModel.getInstance().getResources().add(newResource);

                idField.getScene().getWindow().hide();

            } catch (IllegalArgumentException e) {
                showErrorAlert(e);
                throw new RuntimeException("验证失败", e);
            } catch (SQLException e) {
                showErrorAlert(new Exception("添加操作失败"));
                throw new RuntimeException("数据库错误", e);
            }
        });


    }

    @FXML
    private void handleCancel() {
        idField.getScene().getWindow().hide();
    }



    private ResourceModel validateAndCreateResource(){
        //验证必填字段
        validateRequiredFields();

        //验证价格有效性
        validateRateRange();

        double rate = Double.parseDouble(rateField.getText().trim());

        //创建并返回新资源
        return new ResourceModel(
                nameField.getText().trim(),
                idField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                typeCombo.getValue().trim(),
                rate,
                commentField.getText().trim()

        );

    }

    private void validateRequiredFields(){
        if (nameField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("资源名称不能为空");
        }
        if (idField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("资源ID不能为空");
        }
        if (typeCombo.getValue() == null){
            throw new IllegalArgumentException("必须选择资源类型");
        }
        if (rateField.getText().trim().isEmpty()){
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


    private void showErrorAlert(Exception e) {
        new Alert(Alert.AlertType.ERROR,
                "操作失败：" + e.getMessage(),
                ButtonType.OK).show();
    }



}
