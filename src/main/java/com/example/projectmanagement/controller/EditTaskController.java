package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class EditTaskController {

    @FXML
    public TextField nameField;
    @FXML public TextField idField;
    @FXML public DatePicker startPicker;
    @FXML public DatePicker endPicker;
    @FXML public TextField progressField;
    @FXML public TextField leaderField;
    @FXML public TextArea commentField;
    @FXML public ListView<ResourceModel> resourceListView;//新增资源关联

    private TaskModel taskToEdit;
    private boolean isConfirmed = false;

    public void setTaskToEdit(TaskModel task) {
        this.taskToEdit = task;
        // 将任务数据填充到表单
        nameField.setText(task.getTaskName());
        idField.setText(task.getId());
        startPicker.setValue(task.getStartDate());
        endPicker.setValue(task.getEndDate());
        progressField.setText(String.valueOf(task.getProgress()));
        leaderField.setText(task.getLeader());
        commentField.setText(task.getComment());

        //资源关联
        resourceListView.setItems(DataModel.getInstance().getResources());
        resourceListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resourceListView.getSelectionModel().selectAll();//选中已关联资源
    }

    @FXML
    private void handleOk() {
        try {
            validateAndUpdateTask();
            isConfirmed = true;
            nameField.getScene().getWindow().hide();
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).show();
        }
    }

    @FXML
    private void handleCancel() {
        isConfirmed = false;
        nameField.getScene().getWindow().hide();
    }

    private void validateAndUpdateTask() {
        // 复用 AddTaskController 的验证逻辑
        validateRequiredFields();
        double progress = parseProgress();
        validateDateRange();

        // 更新任务属性
        taskToEdit.setTaskName(nameField.getText().trim());
        taskToEdit.setId(idField.getText().trim());
        taskToEdit.setStartDate(startPicker.getValue());
        taskToEdit.setEndDate(endPicker.getValue());
        taskToEdit.setProgress(progress);
        taskToEdit.setLeader(leaderField.getText().trim());
        taskToEdit.setComment(commentField.getText().trim());


        //更新资源关联
        ObservableList<ResourceModel> selected = resourceListView.getSelectionModel().getSelectedItems();
        updateResourceAssociations(taskToEdit, selected);
    }


    private void updateResourceAssociations(TaskModel task, ObservableList<ResourceModel> newResources){
        //移除旧关联
        for (ResourceModel res : task.getAssignedResources()){
            res.getAssignedTasks().remove(task);
        }
        task.getAssignedResources().clear();


        //添加新关联
        for (ResourceModel res : newResources){
            task.getAssignedResources().add(res);
            if(!res.getAssignedTasks().contains(task)){
                res.getAssignedTasks().add(task);
            }
        }

    }




    // 以下方法与 AddTaskController 相同
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

    public boolean isConfirmed() {
        return isConfirmed;
    }
}

