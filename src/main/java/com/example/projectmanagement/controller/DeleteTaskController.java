package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.TaskModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DeleteTaskController {

    @FXML
    private Label messageLabel;
    private TaskModel taskToDelete;
    private boolean confirmed = false;

    public void setTaskToDelete(TaskModel task) {
        this.taskToDelete = task;
        messageLabel.setText("确定要删除任务 '" + task.getTaskName() + "' 吗？");
    }

    @FXML
    private void handleConfirm() {
        confirmed = true;
        messageLabel.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        messageLabel.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

