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
            ScheduleDAO.delete(scheduleToDelete.getId());
            isConfirmed = true;
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean isConfirmed() {
        return isConfirmed;
    }
}