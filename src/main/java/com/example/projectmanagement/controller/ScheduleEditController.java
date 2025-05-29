package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.ScheduleDAO;
import com.example.projectmanagement.model.ScheduleModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ScheduleEditController {
    @FXML private TextField titleField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea contentField;

    private ScheduleModel selectedSchedule;
    private ScheduleController mainController;

    // 初始化表单数据
    public void setScheduleToEdit(ScheduleModel schedule) {
        this.selectedSchedule = schedule;
        titleField.setText(schedule.getTitle());
        startDatePicker.setValue(schedule.getStartDate());
        endDatePicker.setValue(schedule.getEndDate());
        contentField.setText(schedule.getContent());
    }

    @FXML
    private void handleConfirm() {
        try {
            // 验证数据
            validateRequireFields();

            validateDateRange();

            // 更新模型
            selectedSchedule.setTitle(titleField.getText());
            selectedSchedule.setStartDate(startDatePicker.getValue());
            selectedSchedule.setEndDate(endDatePicker.getValue());
            selectedSchedule.setContent(contentField.getText());

            // 更新数据库
            ScheduleDAO.update(selectedSchedule);

            // 刷新主界面
            mainController.refreshAll();
            closeWindow();
        } catch (Exception e) {
            showAlert("错误", "操作失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void validateRequireFields(){
        if(titleField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("日程标题不能为空");
        }
        if(contentField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("日程内容不能为空");
        }
    }

    private void validateDateRange(){
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) {
            throw new IllegalArgumentException("必须选择开始和结束日期");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }

    private void closeWindow() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setMainController(ScheduleController controller) {
        this.mainController = controller;
    }
}