package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.ResourceDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResourceDeleteController {


    @FXML
    public Label messageLabel;
    public ResourceModel resourceToDelete;
    public boolean isConfirmed = false;


    public void setResourceToDelete(ResourceModel resource){
        this.resourceToDelete = resource;
        messageLabel.setText("确定要删除资源 '" + resource.getId() + "' 吗？");
    }




    @FXML
    private void handleConfirm(){

        DatabaseManager.executeTransaction(() -> {
            try {

                // 删除关联关系
                ResourceDAO.clearResourceTasks(resourceToDelete.getId());
                // 使用DAO删除资源
                ResourceDAO.delete(resourceToDelete.getId());
                // 从任务中删除关联
                DataModel.getInstance().getTasks().forEach(task ->
                        task.getAssignedResources().remove(resourceToDelete)
                );
                // 删除内存数据
                DataModel.getInstance().getResources().remove(resourceToDelete);

                isConfirmed = true;

            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "删除资源失败").show();
                throw new RuntimeException("删除操作失败", e);
            }
        });

        if (isConfirmed) {
            messageLabel.getScene().getWindow().hide();
        }

    }

    @FXML
    private void handleCancel(){
        isConfirmed = false;
        messageLabel.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }



}
