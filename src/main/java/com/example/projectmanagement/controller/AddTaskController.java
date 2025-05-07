package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.TaskModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

//添加新任务
public class AddTaskController {
    @FXML
    public TextField nameField;
    @FXML public TextField idField;
    @FXML public DatePicker startPicker;
    @FXML public DatePicker endPicker;
    @FXML public TextField progressField;
    @FXML public TextField leaderField;
    @FXML public TextArea commentField;



    private TaskModel newTask = null;

    public TaskModel getNewTask() {
        return newTask;
    }


    @FXML
    private void handleOk() {
        try {
            // 执行所有验证并创建任务
            newTask = validateAndCreateTask();
            // 关闭窗口
            nameField.getScene().getWindow().hide();
        } catch (IllegalArgumentException e) {
            // 显示错误提示（保持在当前窗口）
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).show();
        }
    }

    @FXML
    private void handleCancel() {
        nameField.getScene().getWindow().hide();
    }

    private TaskModel validateAndCreateTask() {

        //  用户点击"确定"
        //  → 验证任务名称
        //  → 验证任务ID
        //  → 验证负责人
        //  → 验证进度
        //  → 验证进度范围
        //  → 验证进度格式
        //  → 验证日期有效性
        //  → 全部通过 → 创建 TaskModel
        //  → 任一失败 → 显示错误 → 阻止提交


        // 验证必填字段
        validateRequiredFields();

        // 解析进度
        double progress = parseProgress();

        // 验证日期范围
        validateDateRange();

        // 创建并返回新任务
        return new TaskModel(
                nameField.getText().trim(),
                idField.getText().trim(),
                startPicker.getValue(),
                endPicker.getValue(),
                progress,
                leaderField.getText().trim(),
                commentField.getText().trim()
        );
    }


    private void validateRequiredFields() {
        if (nameField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if (idField.getText().trim().isEmpty()){
            throw new IllegalArgumentException("任务ID不能为空");
        }
        if (leaderField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("负责人不能为空");
        }
        if (progressField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("进度不能为空");
        }
    }




    private double parseProgress() {
        try {
            double progress = Double.parseDouble(progressField.getText().trim());
            if (progress < 0 || progress > 1) {
                throw new IllegalArgumentException("进度必须在0到1之间");
            }
            return progress;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("进度必须为有效数字");
        }
    }




    private void validateDateRange() {
        LocalDate start = startPicker.getValue();
        LocalDate end = endPicker.getValue();
        if (start == null || end == null) {
            throw new IllegalArgumentException("必须选择开始和结束日期");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }







}
