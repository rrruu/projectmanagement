package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.ScheduleDAO;
import com.example.projectmanagement.model.ScheduleModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleController {
//    @FXML private VBox scheduleContainer;
//    @FXML private Button btnNew;
//    @FXML private Button btnDelete;
//    @FXML private Button btnEdit;
//
//    private ToggleGroup toggleGroup = new ToggleGroup();
//
//    @FXML
//    public void initialize() {
//        loadSchedules();
//        setupButtonActions();
//    }
//
//    private void loadSchedules() {
//        scheduleContainer.getChildren().clear();
//        try {
//            List<ScheduleModel> schedules = ScheduleDAO.getAll();
//            for (ScheduleModel schedule : schedules) {
//                scheduleContainer.getChildren().add(createScheduleCard(schedule));
//            }
//        } catch (Exception e) {
//            showAlert("加载失败", e.getMessage());
//        }
//    }
//
//    private ToggleButton createScheduleCard(ScheduleModel schedule) {
//        ToggleButton card = new ToggleButton();
//        card.setToggleGroup(toggleGroup);
//        card.setMaxWidth(Double.MAX_VALUE);
//        card.setStyle("-fx-padding: 10; -fx-border-color: #ccc;");
//
//        String content = String.format("%s\n%s - %s\n%s",
//                schedule.getTitle(),
//                schedule.getStartDate().format(DateTimeFormatter.ISO_DATE),
//                schedule.getEndDate().format(DateTimeFormatter.ISO_DATE),
//                schedule.getContent());
//
//        card.setText(content);
//        return card;
//    }
//
//    private void setupButtonActions() {
//        btnNew.setOnAction(e -> showCreateDialog());
//        btnDelete.setOnAction(e -> deleteSelected());
//        btnEdit.setOnAction(e -> editSelected());
//    }
//
//    private void showCreateDialog() {
//        Dialog<ScheduleModel> dialog = new Dialog<>();
//        dialog.setTitle("新建日程");
//
//        // 加载对话框视图并获取控制器
//        DialogPane dialogPane = ScheduleDialogController.getView(); // 关键修改点
//        dialog.setDialogPane(dialogPane); // 替换整个DialogPane
//
//        ScheduleDialogController controller = ScheduleDialogController.getController(dialogPane); // 获取真实控制器
//
//        // 添加确定/取消按钮（若FXML未定义）
//        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//
//        dialog.setResultConverter(buttonType -> {
//            if (buttonType == ButtonType.OK) {
//                return controller.getSchedule(); // 使用从视图获取的控制器
//            }
//            return null;
//        });
//
//        dialog.showAndWait().ifPresent(this::saveSchedule);
//    }
//
//    private void saveSchedule(ScheduleModel schedule) {
//        DatabaseManager.executeTransaction(() -> {
//            try {
//                ScheduleDAO.insert(schedule);
//                loadSchedules();
//            } catch (SQLException ex) {
//                throw new RuntimeException(ex);
//            }
//        });
//    }
//
//    // 其他方法（删除、编辑）实现类似逻辑
//
//    private void deleteSelected(){
//
//    }
//
//    private void editSelected(){
//
//    }
//
//    private void showAlert(String title, String content) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(content);
//        alert.showAndWait();
//    }


}