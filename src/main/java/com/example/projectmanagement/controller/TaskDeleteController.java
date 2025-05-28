package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.TaskDAO;
import com.example.projectmanagement.model.TaskModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import com.example.projectmanagement.model.DataModel;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaskDeleteController {

    @FXML
    public Label messageLabel;
    public TaskModel taskToDelete;
    public boolean isConfirmed = false;

    public void setTaskToDelete(TaskModel task) {
        this.taskToDelete = task;
        messageLabel.setText("确定要删除任务 '" + task.getTaskName() + "' 吗？");
    }

    @FXML
    private void handleConfirm() {
        DatabaseManager.executeTransaction(() -> {
            try {
                // 删除关联关系
                TaskDAO.clearTaskResources(taskToDelete.getId());
                // 使用DAO删除任务
                TaskDAO.delete(taskToDelete.getId());
                // 从资源中删除关联
                DataModel.getInstance().getResources().forEach(res ->
                        res.getAssignedTasks().remove(taskToDelete)
                );

                // 删除内存数据
                DataModel.getInstance().getTasks().remove(taskToDelete);
                isConfirmed = true;
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "删除任务失败").show();
                throw new RuntimeException("删除操作失败", e);
            }
        });

        if (isConfirmed) {
            messageLabel.getScene().getWindow().hide();
        }
    }

    @FXML
    private void handleCancel() {
        isConfirmed = false;
        messageLabel.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }





}

