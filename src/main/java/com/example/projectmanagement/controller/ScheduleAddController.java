package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.ScheduleDAO;
import com.example.projectmanagement.model.ScheduleModel;
import javafx.application.Platform;
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
    private void handleConfirm() {
        try {

            validateRequireFields();

            validateDateRange();

            ScheduleModel schedule = new ScheduleModel(
                    UUID.randomUUID().toString(),
                    titleField.getText(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue(),
                    contentField.getText()
            );



            ScheduleDAO.create(schedule);


            // 仅调用一次刷新，且确保在主线程
            Platform.runLater(() -> {
                mainController.refreshAll();
            });
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
