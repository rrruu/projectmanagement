package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.TaskDAO;
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

public class LinkResourceController {
    @FXML
    private ListView<ResourceModel> resourceListView;

    private TaskModel currentTask;

    public void setCurrentTask(TaskModel task) {
        this.currentTask = task;
        resourceListView.setItems(DataModel.getInstance().getResources());



        // 设置自定义的CellFactory
        resourceListView.setCellFactory(new Callback<ListView<ResourceModel>, ListCell<ResourceModel>>() {
            @Override
            public ListCell<ResourceModel> call(ListView<ResourceModel> param) {
                return new ListCell<ResourceModel>() {
                    private final CheckBox checkBox = new CheckBox();

                    {
                        //复选框状态改变时更新选择状态
                        checkBox.setOnAction(event -> {
                            ResourceModel item = getItem();
                            if (item != null) {
                                if (checkBox.isSelected()) {
                                    resourceListView.getSelectionModel().select(item);
                                } else {
                                    resourceListView.getSelectionModel().clearSelection(resourceListView.getItems().indexOf(item));
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(ResourceModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);

                        } else {


                            //设置资源显示文本
                            checkBox.setText(item.getName() + " (" + item.getId() + ")");
                            checkBox.setSelected(resourceListView.getSelectionModel().getSelectedItems().contains(item));


                            boolean isAvailable = isResourceAvailable(item);
                            //设置不可用状态
                            checkBox.setDisable(!isAvailable);
                            setDisable(!isAvailable); // 整行不可选

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
        resourceListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        //预选中已关联资源
        currentTask.getAssignedResources().forEach(res ->
                resourceListView.getSelectionModel().select(res)
        );
    }



    @FXML
    private void handleSelectAll() {
        resourceListView.getSelectionModel().selectAll();
    }

    @FXML
    private void handleDeselectAll() {
        resourceListView.getSelectionModel().clearSelection();
    }


    @FXML
    private void handleConfirm() {

        //1.获取选中的资源
        ObservableList<ResourceModel> selected = resourceListView.getSelectionModel().getSelectedItems();

        //2.验证选中的资源是否全部可用
        List<ResourceModel> invalidResources = new ArrayList<>();
        for (ResourceModel res : selected) {
            if (!isResourceAvailable(res)) {
                invalidResources.add(res);
            }
        }

        //3.存在不可用资源时提示
        if (!invalidResources.isEmpty()) {
            showConflictAlert(invalidResources);
            return;
        }

        //4.更新数据库关联
        updateResourceAssociations(currentTask, selected);


        //5.更新双向关联
        updateBidirectionalAssociations(selected);


        //6.关闭窗口
        resourceListView.getScene().getWindow().hide();

        //7.刷新UI
        refreshUI();
    }

    @FXML
    private void handleCancel() {
        resourceListView.getScene().getWindow().hide();
    }




    //更新数据库关联
    private void updateResourceAssociations(TaskModel task, ObservableList<ResourceModel> newResources) {
        DatabaseManager.executeTransaction(() -> {
            try {
                //使用DAO处理关联
                //清除旧关联
                TaskDAO.clearTaskResources(task.getId());
                //添加新关联
                TaskDAO.addTaskResources(task.getId(), newResources);
            } catch (SQLException e) {
                throw new RuntimeException("关联更新失败", e);
            }
        });
    }



    private void updateBidirectionalAssociations(ObservableList<ResourceModel> selectedResources) {
        //从所有资源中移除当前任务
        DataModel.getInstance().getResources().forEach(res -> {
            res.getAssignedTasks().remove(currentTask);
        });
        //将当前任务添加到选中的资源中
        selectedResources.forEach(res -> {
            if (!res.getAssignedTasks().contains(currentTask)) {
                res.getAssignedTasks().add(currentTask);
            }
        });
        //更新当前任务的关联资源
        currentTask.getAssignedResources().setAll(selectedResources);
    }



    private void refreshUI() {
        // 强制刷新资源表（触发列表更新）
        DataModel.getInstance().getResources().forEach(r -> {
            int index = DataModel.getInstance().getResources().indexOf(r);
            DataModel.getInstance().getResources().set(index, r);
        });

        // 强制刷新任务表
        DataModel.getInstance().getTasks().forEach(t -> {
            int index = DataModel.getInstance().getTasks().indexOf(t);
            DataModel.getInstance().getTasks().set(index, t);
        });
    }






    private ObservableList<ResourceModel> getAvailableResources() {
        ObservableList<ResourceModel> availableResources = FXCollections.observableArrayList();

        for (ResourceModel res : DataModel.getInstance().getResources()) {
            if (isResourceAvailable(res)) {
                availableResources.add(res);
            }
        }
        return availableResources;
    }



    //资源可用性检查
    private boolean isResourceAvailable(ResourceModel resource) {
        // 获取该资源所有已关联任务，检查当前任务是否与资源已关联任务存在冲突
        for (TaskModel associatedTask : resource.getAssignedTasks()) {
            if (associatedTask != currentTask && // 排除当前任务自身
                    TimeConflictChecker.hasTimeConflict(currentTask, associatedTask)) {
                return false;
            }
        }
        return true;
    }





    @FXML
    private void showAllResources(){
        resourceListView.setItems(DataModel.getInstance().getResources());
        // 选中已关联资源
        currentTask.getAssignedResources().forEach(res ->
                resourceListView.getSelectionModel().select(res)
        );
    }

    @FXML
    private void showAvailableResoreces(){
        resourceListView.setItems(getAvailableResources());
        // 选中已关联资源
        currentTask.getAssignedResources().forEach(res ->
                resourceListView.getSelectionModel().select(res)
        );
    }



    // 冲突提示方法
    private void showConflictAlert(List<ResourceModel> invalidResources) {
        StringBuilder message = new StringBuilder("以下资源存在时间冲突：\n");
        for (ResourceModel res : invalidResources) {
            message.append("• ")
                    .append(res.getName())
                    .append(" (")
                    .append(res.getId())
                    .append(")\n");
        }
        message.append("\n请点击“显示可用资源”选择可用资源");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("时间冲突警告");
        alert.setHeaderText("关联操作失败，存在不可用的资源选择");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }



}