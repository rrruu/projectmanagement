package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.ScheduleModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ScheduleShowController {
    @FXML private Label titleLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private TextArea contentArea;

    private ScheduleModel schedule;

    // 初始化时绑定数据到 UI 组件
    public void setSchedule(ScheduleModel schedule) {
        this.schedule = schedule;
        titleLabel.setText(schedule.getTitle());
        startDateLabel.setText(schedule.getStartDate().toString());
        endDateLabel.setText(schedule.getEndDate().toString());
        contentArea.setText(schedule.getContent());
    }

    @FXML
    private void handleClose() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}