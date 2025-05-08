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
    @FXML public ListView<TaskModel> taskListView;//新增任务关联


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
//        taskListView.setItems(resource.getAssignedTasks());


        // 初始化任务列表
        taskListView.setItems(DataModel.getInstance().getTasks());
        taskListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 选中已关联任务
        taskListView.getSelectionModel().selectAll();
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





//            //更新任务属性
//            resourceToEdit.setName(nameField.getText().trim());
//            resourceToEdit.setId(idField.getText().trim());
//            resourceToEdit.setPhone(phoneField.getText().trim());
//            resourceToEdit.setEmail(emailField.getText().trim());
//            resourceToEdit.setType(typeCombo.getValue().trim());
//            resourceToEdit.setDailyRate(rate);
//            resourceToEdit.setComment(commentField.getText().trim());
//
//            // 更新任务关联
//            ObservableList<TaskModel> selected = taskListView.getSelectionModel().getSelectedItems();
//            updateTaskAssociations(resourceToEdit, selected);
//
//
//            updateResourceInDatabase(resourceToEdit); //更新数据库
//            updateTaskAssociations(resourceToEdit,selected);
//
//
//            DatabaseManager.getConnection().commit();
//            DataModel.getInstance().loadAllData();//重新加载数据库

            // 更新资源基本信息
            updateResourceFields();

            //使用DAO更新资源
            ResourceDAO.update(resourceToEdit);

            // 处理任务关联
            ObservableList<TaskModel> selected = taskListView.getSelectionModel().getSelectedItems();
            updateTaskAssociations(selected);

            // 增量刷新数据
            DataModel.getInstance().loadResources();
            DataModel.getInstance().loadAssociations();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "数据库操作失败").show();
            rollbackTransaction();
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


    private void updateResourceInDatabase(ResourceModel resource) throws SQLException {
        String sql = "UPDATE resources SET name=?, phone=?, email=?, type=?, daily_rate=?, status=?, comment=? " +
                "WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, resource.getName());
            stmt.setString(2, resource.getPhone());
            stmt.setString(3, resource.getEmail());
            stmt.setString(4, resource.getType());
            stmt.setDouble(5, resource.getDailyRate());
            stmt.setString(6, resource.getStatus());
            stmt.setString(7, resource.getComment());
            stmt.setString(8, resource.getId());
            stmt.executeUpdate();
        }
    }





    private void updateTaskAssociations(ObservableList<TaskModel> newTasks) {
        DatabaseManager.executeTransaction(() -> {
            try {
                // 清除旧关联
                ResourceDAO.clearResourceTasks(resourceToEdit.getId());

                // 添加新关联
                ResourceDAO.addResourceTasks(resourceToEdit.getId(), newTasks);

            } catch (SQLException e) {
                throw new RuntimeException("关联更新失败", e);
            }
        });
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
