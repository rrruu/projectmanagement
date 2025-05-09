package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.TaskDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import com.example.projectmanagement.db.ResourceDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;

import java.sql.SQLException;

public class LinkTaskController {
    @FXML
    private ListView<TaskModel> taskListView;

    private ResourceModel currentResource;

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
                        } else {
                            checkBox.setText(item.getTaskName() + " (" + item.getId() + ")");
                            checkBox.setSelected(taskListView.getSelectionModel().getSelectedItems().contains(item));
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

}