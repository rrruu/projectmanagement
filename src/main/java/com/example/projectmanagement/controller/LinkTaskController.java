package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.TaskDAO;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import com.example.projectmanagement.db.ResourceDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.sql.SQLException;

public class LinkTaskController {
    @FXML
    private ListView<TaskModel> taskListView;

    private ResourceModel currentResource;

    public void setCurrentResource(ResourceModel resource) {
        this.currentResource = resource;
        taskListView.setItems(DataModel.getInstance().getTasks());
        taskListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 选中已关联任务
//        taskListView.getSelectionModel().selectAll();
        currentResource.getAssignedTasks().forEach(task ->
                taskListView.getSelectionModel().select(task)
        );
    }

    @FXML
    private void handleConfirm() {
        ObservableList<TaskModel> selected = taskListView.getSelectionModel().getSelectedItems();
        updateTaskAssociations(currentResource, selected);


        // 更新双向关联
        selected.forEach(task -> {
            if (!task.getAssignedResources().contains(currentResource)) {
                task.getAssignedResources().add(currentResource);
            }
        });


        // 移除未选中的关联
        DataModel.getInstance().getTasks().forEach(task -> {
            if (!selected.contains(task)) {
                task.getAssignedResources().remove(currentResource);
            }
        });

        //将当前资源的关联任务替换为列表中选中的关联任务
        currentResource.getAssignedTasks().setAll(selected);


        taskListView.getScene().getWindow().hide();


        // 强制刷新任务表
        DataModel.getInstance().getTasks().forEach(t -> {
            int index = DataModel.getInstance().getTasks().indexOf(t);
            DataModel.getInstance().getTasks().set(index, t);
        });
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
}