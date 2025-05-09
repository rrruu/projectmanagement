package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.TaskDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import com.example.projectmanagement.db.ResourceDAO;
import com.example.projectmanagement.util.TimeConflictChecker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LinkTaskController {
    @FXML
    private ListView<TaskModel> taskListView;

    private ResourceModel currentResource;

    private boolean showAvailableOnly = false;

    public void setCurrentResource(ResourceModel resource) {
        this.currentResource = resource;
        taskListView.setItems(DataModel.getInstance().getTasks());
//        taskListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 设置自定义的CellFactory
        taskListView.setCellFactory(new Callback<ListView<TaskModel>, ListCell<TaskModel>>() {
            @Override
            public ListCell<TaskModel> call(ListView<TaskModel> param) {
                return new ListCell<TaskModel>() {
                    private final CheckBox checkBox = new CheckBox();

                    {
                        checkBox.setOnAction(event -> {
                            TaskModel item = getItem();
                            if (item != null) {
                                if (checkBox.isSelected()) {
                                    taskListView.getSelectionModel().select(item);
                                } else {
                                    taskListView.getSelectionModel().clearSelection(taskListView.getItems().indexOf(item));
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(TaskModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
//                            setTooltip(null);
                        } else {
                            boolean isAvailable = isTaskAvailable(item);

                            checkBox.setText(item.getTaskName() + " (" + item.getId() + ")");
                            checkBox.setSelected(taskListView.getSelectionModel().getSelectedItems().contains(item));


                            // 设置不可用状态
                            checkBox.setDisable(!isAvailable);
                            setDisable(!isAvailable);
                            if (!isAvailable) {
                                setStyle("-fx-opacity: 0.6; -fx-background-color: #ffeeee;");
//                                Tooltip tooltip = new Tooltip("该任务存在时间冲突");
//
//                                checkBox.setTooltip(tooltip);
//                                setTooltip(tooltip);
                            } else {
                                setStyle("");
//                                checkBox.setTooltip(null);//清除提示
//                                setTooltip(null);
                            }


                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });

        taskListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);


        // 选中已关联任务
        currentResource.getAssignedTasks().forEach(task ->
                taskListView.getSelectionModel().select(task)
        );
    }


    @FXML
    private void handleSelectAll() {
        taskListView.getSelectionModel().selectAll();
    }

    @FXML
    private void handleDeselectAll() {
        taskListView.getSelectionModel().clearSelection();
    }



    @FXML
    private void handleConfirm() {
        ObservableList<TaskModel> selected = taskListView.getSelectionModel().getSelectedItems();



        // 验证选中的任务是否全部可用
        List<TaskModel> invalidTasks = new ArrayList<>();
        for (TaskModel task : selected) {
            if (!isTaskAvailable(task)) {
                invalidTasks.add(task);
            }
        }

        if (!invalidTasks.isEmpty()) {
            showConflictAlert(invalidTasks);
            return;
        }


        //更新数据库关联
        updateTaskAssociations(currentResource, selected);


        // 更新双向关联
        updateBidirectionalAssociations(selected);





        //关闭窗口
        taskListView.getScene().getWindow().hide();


        // 刷新UI
        refreshUI();
    }

    @FXML
    private void handleCancel() {
        taskListView.getScene().getWindow().hide();
    }

    private void updateTaskAssociations(ResourceModel resource,ObservableList<TaskModel> newTasks) {
        DatabaseManager.executeTransaction(() -> {
            try {
                // 清除旧关联
                ResourceDAO.clearResourceTasks(resource.getId());

                // 添加新关联
                ResourceDAO.addResourceTasks(resource.getId(), newTasks);

            } catch (SQLException e) {
                throw new RuntimeException("关联更新失败", e);
            }
        });
    }



    private void updateBidirectionalAssociations(ObservableList<TaskModel> selectedTasks) {
        // 1. 从所有任务中移除当前资源（如果存在）
        DataModel.getInstance().getTasks().forEach(task -> {
            task.getAssignedResources().remove(currentResource);
        });

        // 2. 将当前资源添加到选中的任务中
        selectedTasks.forEach(task -> {
            if (!task.getAssignedResources().contains(currentResource)) {
                task.getAssignedResources().add(currentResource);
            }
        });

        // 3. 更新当前资源的关联任务
        currentResource.getAssignedTasks().setAll(selectedTasks);
    }

    private void refreshUI() {
        // 强制刷新任务表
        DataModel.getInstance().getTasks().forEach(t -> {
            int index = DataModel.getInstance().getTasks().indexOf(t);
            DataModel.getInstance().getTasks().set(index, t);
        });

        // 强制刷新资源表
        DataModel.getInstance().getResources().forEach(r -> {
            int index = DataModel.getInstance().getResources().indexOf(r);
            DataModel.getInstance().getResources().set(index, r);
        });
    }


    private ObservableList<TaskModel> getAvailableTasks() {
        ObservableList<TaskModel> availableTasks = FXCollections.observableArrayList();

        for (TaskModel task : DataModel.getInstance().getTasks()) {
            if (isTaskAvailable(task)) {
                availableTasks.add(task);
            }
        }
        return availableTasks;
    }

    private boolean isTaskAvailable(TaskModel task) {
        // 检查是否与当前资源已有任务冲突
        for (TaskModel assignedTask : currentResource.getAssignedTasks()) {
            if (assignedTask != task && // 排除当前任务自身
                    TimeConflictChecker.hasTimeConflict(task, assignedTask)) {
                return false;
            }
        }
        return true;
    }




    @FXML
    private void showAllTasks(){
        taskListView.setItems(DataModel.getInstance().getTasks());
        // 选中已关联任务
        currentResource.getAssignedTasks().forEach(task ->
                taskListView.getSelectionModel().select(task)
        );
    }

    @FXML
    private void showAvailableTasks(){
        taskListView.setItems(getAvailableTasks());
        // 选中已关联任务
        currentResource.getAssignedTasks().forEach(task ->
                taskListView.getSelectionModel().select(task)
        );
    }



    // 新增冲突提示方法
    private void showConflictAlert(List<TaskModel> invalidTasks) {
        StringBuilder message = new StringBuilder("以下任务存在时间冲突：\n");
        for (TaskModel task : invalidTasks) {
            message.append("• ").append(task.getTaskName()).append(" (").append(task.getId()).append(")\n");
        }
        message.append("\n请切换到'可用任务'视图选择可用任务");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("时间冲突警告");
        alert.setHeaderText("存在不可用的任务选择");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }


}