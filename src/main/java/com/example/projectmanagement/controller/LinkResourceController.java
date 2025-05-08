package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import com.example.projectmanagement.db.TaskDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.sql.SQLException;

public class LinkResourceController {
    @FXML
    private ListView<ResourceModel> resourceListView;

    private TaskModel currentTask;

    public void setCurrentTask(TaskModel task) {
        this.currentTask = task;
        resourceListView.setItems(DataModel.getInstance().getResources());
        resourceListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 选中已关联资源
        resourceListView.getSelectionModel().selectAll();
        currentTask.getAssignedResources().forEach(res ->
                resourceListView.getSelectionModel().select(res)
        );
    }

    @FXML
    private void handleConfirm() {
        ObservableList<ResourceModel> selected = resourceListView.getSelectionModel().getSelectedItems();
        updateResourceAssociations(currentTask, selected);
        currentTask.getAssignedResources().setAll(selected);
        resourceListView.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        resourceListView.getScene().getWindow().hide();
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
}