package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.TaskDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
//    @FXML public ListView<ResourceModel> resourceListView;//新增资源关联

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

//        //资源关联
//        resourceListView.setItems(DataModel.getInstance().getResources());
//        resourceListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        resourceListView.getSelectionModel().selectAll();//选中已关联资源
    }

    @FXML
    private void handleOk() {
        DatabaseManager.executeTransaction(() -> {
            try {
                validateAndUpdateTask();
                isConfirmed = true;
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                throw new RuntimeException("任务更新失败", e);
            }
        });

        if (isConfirmed) {
            idField.getScene().getWindow().hide();
        }
    }

    @FXML
    private void handleCancel() {
        isConfirmed = false;
        nameField.getScene().getWindow().hide();
    }

    private void validateAndUpdateTask() {

        try {
            // 复用 AddTaskController 的验证逻辑
            validateRequiredFields();

            validateDateRange();


            //更新任务基本信息
            updateTaskFields();
            // 使用DAO更新任务
            TaskDAO.update(taskToEdit);

//            //更新资源关联
//            ObservableList<ResourceModel> selected = resourceListView.getSelectionModel().getSelectedItems();
//            updateResourceAssociations(taskToEdit, selected);


//            // 直接更新内存中的关联，避免重新加载数据库
//            taskToEdit.getAssignedResources().setAll(selected);
//            selected.forEach(res -> {
//                if (!res.getAssignedTasks().contains(taskToEdit)) {
//                    res.getAssignedTasks().add(taskToEdit);
//                }
//            });


            // 增量刷新数据
            DataModel.getInstance().loadResources();
            DataModel.getInstance().loadAssociations();


        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "数据库操作失败").show();
            rollbackTransaction();
        }
    }



    private void updateTaskFields(){
        double progress = parseProgress();

        taskToEdit.setTaskName(nameField.getText().trim());
        taskToEdit.setId(idField.getText().trim());
        taskToEdit.setStartDate(startPicker.getValue());
        taskToEdit.setEndDate(endPicker.getValue());
        taskToEdit.setProgress(progress);
        taskToEdit.setLeader(leaderField.getText().trim());
        taskToEdit.setComment(commentField.getText().trim());
    }


    private void updateTaskInDatabase(TaskModel task) throws SQLException {
        String sql = "UPDATE tasks SET name=?, start_date=?, end_date=?, progress=?, leader=?, comment=? " +
                "WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, task.getTaskName());
            stmt.setString(2, task.getStartDate().toString());
            stmt.setString(3, task.getEndDate().toString());
            stmt.setDouble(4, task.getProgress());
            stmt.setString(5, task.getLeader());
            stmt.setString(6, task.getComment());
            stmt.setString(7, task.getId());
            stmt.executeUpdate();
        }
    }







    private void updateResourceAssociations(TaskModel task, ObservableList<ResourceModel> newResources) {
        DatabaseManager.executeTransaction(() -> {
            try {
                // 使用DAO处理关联
                TaskDAO.clearTaskResources(task.getId());
                TaskDAO.addTaskResources(task.getId(), newResources);
            } catch (SQLException e) {
                throw new RuntimeException("关联更新失败", e);
            }
        });
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

