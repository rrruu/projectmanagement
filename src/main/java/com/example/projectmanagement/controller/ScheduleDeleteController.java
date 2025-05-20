package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.ScheduleDAO;
import com.example.projectmanagement.model.ScheduleModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ScheduleDeleteController {
    @FXML public Label messageLabel;
    private ScheduleModel scheduleToDelete;
    private boolean isConfirmed = false;

    public void setScheduleToDelete(ScheduleModel schedule) {
        this.scheduleToDelete = schedule;
        messageLabel.setText("确定要删除日程 '" + schedule.getTitle() + "' 吗？");
    }

    @FXML
    private void handleConfirm() {
        try {
            if (scheduleToDelete != null) {
                ScheduleDAO.delete(scheduleToDelete.getId());
                isConfirmed = true;
            }
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("删除失败: " + e.getMessage());
        }
    }


    @FXML
    private void handleCancel() {
        isConfirmed = false;
        closeWindow();
    }

    private void closeWindow() {
        messageLabel.getScene().getWindow().hide();
    }

    private void showErrorAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public boolean isConfirmed() {
        return isConfirmed;
    }
}