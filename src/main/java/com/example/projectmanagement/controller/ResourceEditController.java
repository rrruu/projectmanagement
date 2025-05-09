package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.ResourceDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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

        //将资源数据填充到表单
        nameField.setText(resource.getName());
        idField.setText(resource.getId());
        phoneField.setText(resource.getPhone());
        emailField.setText(resource.getEmail());
        typeCombo.setValue(resource.getType());
        rateField.setText(String.valueOf(resource.getDailyRate()));
        commentField.setText(resource.getComment());

    }


    @FXML
    private void handleConfirm(){
        DatabaseManager.executeTransaction(() -> {
            try {
                validateAndUpdateResource();
                isConfirmed = true;
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                throw new RuntimeException("资源更新失败", e);
            }
        });

        if (isConfirmed) {
            idField.getScene().getWindow().hide();
        }
    }



    @FXML
    private void handleCancel() {
        isConfirmed = false;
        idField.getScene().getWindow().hide();
    }



    private void validateAndUpdateResource(){


        try {
            //验证必填字段
            validateRequiredFields();

            //验证价格有效性
            validateRateRange();



            // 更新资源基本信息
            updateResourceFields();

            //使用DAO更新资源
            ResourceDAO.update(resourceToEdit);


            // 增量刷新数据
            DataModel.getInstance().loadResources();
            DataModel.getInstance().loadAssociations();

        } catch (SQLException e) {
            rollbackTransaction();
            new Alert(Alert.AlertType.ERROR, "数据库操作失败").show();
            throw new RuntimeException("数据库操作失败", e); // 再抛出，交给 executeTransaction 做二次处理
        }


    }



    private void updateResourceFields() {
        double rate = Double.parseDouble(rateField.getText().trim());


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




    public boolean isConfirmed() {
        return isConfirmed;
    }


    private void showErrorAlert(Exception e) {
        new Alert(Alert.AlertType.ERROR,
                "操作失败：" + e.getMessage(),
                ButtonType.OK).show();
    }

    private void rollbackTransaction() {
        try {
            DatabaseManager.getConnection().rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }




}
