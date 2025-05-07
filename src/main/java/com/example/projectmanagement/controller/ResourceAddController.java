package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.PreparedStatement;
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

    public ResourceModel getNewResource(){ return newResource;}

    private boolean confirmed = false;


    @FXML
    private void initialize() {
        typeCombo.getItems().addAll("人力", "设备", "场地");
    }

//    public void setResource(ResourceModel resource) {
//        this.resource = resource;
//        nameField.setText(resource.getName());
//        idField.setText(resource.getId());
//        phoneField.setText(resource.getPhone());
//        emailField.setText(resource.getEmail());
//        typeCombo.setValue(resource.getType());
//        rateField.setText(String.valueOf(resource.getDailyRate()));
//    }
//
//    public ResourceModel getResource() {
//        if (resource == null) {
//            resource = new ResourceModel();
//        }
//        resource.setName(nameField.getText());
//        resource.setId(idField.getText());
//        resource.setPhone(phoneField.getText());
//        resource.setEmail(emailField.getText());
//        resource.setType(typeCombo.getValue());
//        try {
//            resource.setDailyRate(Double.parseDouble(rateField.getText()));
//        } catch (NumberFormatException e) {
//            resource.setDailyRate(0.0);
//        }
//        return resource;
//    }

    @FXML
    private void handleConfirm() {
        try{
            //执行所有验证并创建任务
            newResource = validateAndCreateResource();
            saveResourceToDatabase(newResource);//数据库保存
            DatabaseManager.getConnection().commit();//提交事务
            DataModel.getInstance().loadResources();//重新加载数据
            nameField.getScene().getWindow().hide();


            //关闭窗口
            idField.getScene().getWindow().hide();
        } catch (IllegalArgumentException e) {
            showErrorAlert(e);
            rollbackTransaction();
        } catch (SQLException e) {
            showErrorAlert(new Exception("数据库操作失败：" + e.getMessage()));
            rollbackTransaction();
        }

        confirmed = true;
        nameField.getScene().getWindow().hide();
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

    private void saveResourceToDatabase(ResourceModel resource) throws SQLException {
        String sql = "INSERT INTO tasks(id, name, phone, email, type, daily_rate, status, comment) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, resource.getId());
            stmt.setString(2, resource.getName());
            stmt.setString(3, resource.getPhone());
            stmt.setString(4, resource.getEmail());
            stmt.setString(5, resource.getType());
            stmt.setDouble(6, resource.getDailyRate());
            stmt.setString(7, resource.getStatus());
            stmt.setString(8, resource.getComment());
            stmt.executeUpdate();
        }
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

//    public boolean isConfirmed() {
//        return confirmed;
//    }
}
