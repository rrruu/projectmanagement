package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.ScheduleDAO;
import com.example.projectmanagement.model.ScheduleModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.UUID;

public class ScheduleAddController {
    @FXML private TextField titleField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea contentField;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private ScheduleController mainController;

    @FXML
    private void initialize() {
        confirmButton.setOnAction(e -> handleConfirm());
        cancelButton.setOnAction(e -> handleCancel());
    }

    private void handleConfirm() {
        try {
            ScheduleModel schedule = new ScheduleModel(
                    UUID.randomUUID().toString(),
                    titleField.getText(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue(),
                    contentField.getText()
            );

            if (schedule.getStartDate().isAfter(schedule.getEndDate())) {
                showAlert("错误", "开始时间不能晚于结束时间");
                return;
            }

            ScheduleDAO.create(schedule);
            mainController.refreshAll();
            closeWindow();
        } catch (Exception e) {
            showAlert("错误", "保存失败: " + e.getMessage());
        }
    }

    private void handleCancel() {
        closeWindow();
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
