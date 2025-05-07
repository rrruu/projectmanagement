package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.model.TaskModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import com.example.projectmanagement.model.DataModel;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteTaskController {

    @FXML
    public Label messageLabel;
    public TaskModel taskToDelete;
    public boolean confirmed = false;

    public void setTaskToDelete(TaskModel task) {
        this.taskToDelete = task;
        messageLabel.setText("确定要删除任务 '" + task.getTaskName() + "' 吗？");
    }

    @FXML
    private void handleConfirm() {
        try {
            deleteTaskFromDatabase(taskToDelete);
            deleteTaskAssociations(taskToDelete);
            DatabaseManager.getConnection().commit();//提交事务
            DataModel.getInstance().loadAllData(); // 重新加载数据
            confirmed = true;
            messageLabel.getScene().getWindow().hide();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "删除失败").show();
            rollbackTransaction();
        }
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        messageLabel.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return confirmed;
    }


    private void deleteTaskFromDatabase(TaskModel task) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, task.getId());
            stmt.executeUpdate();
        }
    }

    private void deleteTaskAssociations(TaskModel task) throws SQLException {
        String sql = "DELETE FROM task_resources WHERE task_id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, task.getId());
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
}

