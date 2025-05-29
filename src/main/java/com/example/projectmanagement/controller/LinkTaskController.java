package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.ResourceDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
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


    public void setCurrentResource(ResourceModel resource) {
        this.currentResource = resource;
        taskListView.setItems(DataModel.getInstance().getTasks());


        // 设置自定义的CellFactory
        taskListView.setCellFactory(new Callback<ListView<TaskModel>, ListCell<TaskModel>>() {
            @Override
            public ListCell<TaskModel> call(ListView<TaskModel> param) {
                return new ListCell<TaskModel>() {
                    private final CheckBox checkBox = new CheckBox();

                    {
                        //复选框状态改变时更新选择状态
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

                        } else {

                            //设置任务显示文本
                            checkBox.setText(item.getTaskName() + " (" + item.getId() + "  " + item.getStartDate() + "-" + item.getEndDate() + ")");
                            checkBox.setSelected(taskListView.getSelectionModel().getSelectedItems().contains(item));

                            boolean isAvailable = isTaskAvailable(item);
                            //设置不可用状态
                            checkBox.setDisable(!isAvailable);
                            setDisable(!isAvailable);//整行不可选

                            //不可用资源样式设置
                            if (!isAvailable) {
                                setStyle("-fx-opacity: 0.6; -fx-background-color: #ffeeee;");
                            } else {
                                setStyle("");

                            }


                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });

        //启用多选模式
        taskListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);


        //预选中已关联任务
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

        //1.获取选中的任务
        ObservableList<TaskModel> selected = taskListView.getSelectionModel().getSelectedItems();

        //2.检查选中的任务之间是否存在冲突
        if (TimeConflictChecker.hasTimeConflictInList(selected)) {
            showConflictAlert(selected, "选中的任务之间存在时间冲突！");
            return;
        }

        //3.验证选中的任务是否全部可用
        List<TaskModel> invalidTasks = new ArrayList<>();
        for (TaskModel task : selected) {
            if (!isTaskAvailable(task)) {
                invalidTasks.add(task);
            }
        }


        //4.存在不可分配任务时提示
        if (!invalidTasks.isEmpty()) {
            showConflictAlert(invalidTasks, "以下任务与资源已有任务冲突：");
            return;
        }


        //5.更新数据库关联
        updateTaskAssociations(currentResource, selected);

        //6.更新双向关联
        updateBidirectionalAssociations(selected);

        //7.关闭窗口
        taskListView.getScene().getWindow().hide();

        //8.刷新UI
        refreshUI();
    }

    @FXML
    private void handleCancel() {
        taskListView.getScene().getWindow().hide();
    }

    private void updateTaskAssociations(ResourceModel resource,ObservableList<TaskModel> newTasks) {
        DatabaseManager.executeTransaction(() -> {
            try {
                //使用DAO处理关联
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
        //从所有任务中移除当前资源（如果存在）
        DataModel.getInstance().getTasks().forEach(task -> {
            task.getAssignedResources().remove(currentResource);
        });

        //将当前资源添加到选中的任务中
        selectedTasks.forEach(task -> {
            if (!task.getAssignedResources().contains(currentResource)) {
                task.getAssignedResources().add(currentResource);
            }
        });

        //更新当前资源的关联任务
        currentResource.getAssignedTasks().setAll(selectedTasks);
    }

    private void refreshUI() {
        // 强制刷新任务表（触发列表更新）
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

    //任务可用性检查
    private boolean isTaskAvailable(TaskModel task) {
        // 检查当前资源已关联任务是否与任务存在冲突
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






    // 冲突提示方法，支持两种类型的冲突
    private void showConflictAlert(List<TaskModel> conflictTasks, String header) {
        StringBuilder message = new StringBuilder(header + "\n");
        for (TaskModel task : conflictTasks) {
            message.append("• ")
                    .append(task.getTaskName())
                    .append(" (")
                    .append(task.getStartDate())
                    .append(" - ")
                    .append(task.getEndDate())
                    .append(")\n");
        }
        message.append("\n请点击“显示可用任务”选择可用任务");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("时间冲突警告");
        alert.setHeaderText("关联操作失败，存在不可用的任务选择");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }



}