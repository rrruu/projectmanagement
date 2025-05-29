// DataModel.java
package com.example.projectmanagement.model;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.ResourceDAO;
import com.example.projectmanagement.db.TaskDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataModel {
    private static DataModel instance = new DataModel();

    private ObservableList<TaskModel> tasks = FXCollections.observableArrayList();
    private ObservableList<ResourceModel> resources = FXCollections.observableArrayList();


    //资源统计分析界面图表3和4 时间选择器默认值
    private LocalDate analysisStartDate = LocalDate.of(2025, 1, 1);
    private LocalDate analysisEndDate = LocalDate.of(2025, 12, 31);
    private DataModel() {
        loadAllData();
    } // 私有构造器

    public static DataModel getInstance() {
        return instance;
    }

    //从数据库加载所有数据
    public void loadAllData(){
        loadTasks();
        loadResources();
        loadAssociations();
    }

    //加载任务
    public void loadTasks(){
        tasks.clear();
        try {
            List<TaskModel> newTasks = TaskDAO.findAll();
            tasks.setAll(newTasks);// 此处会触发 ObservableList 的监听器
            loadAssociations(); // 加载关联关系
        } catch (SQLException e) {
            throw new DataLoadingException("Failed to load tasks", e);
        }
    }

    //加载资源
    public void loadResources() {
        resources.clear();
        try {
            List<ResourceModel> newResources = ResourceDAO.findAll();
            resources.setAll(newResources);
            loadAssociations();
        } catch (SQLException e) {
            throw new DataLoadingException("Failed to load resources", e);
        }
    }

    //加载关联关系
    public void loadAssociations() {
        //先清空现有关联
        tasks.forEach(task -> task.getAssignedResources().clear());
        resources.forEach(res -> res.getAssignedTasks().clear());

        //重新加载数据库关联
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM task_resources")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String taskId = rs.getString("task_id");
                String resourceId = rs.getString("resource_id");
                TaskModel task = findTaskById(taskId);
                ResourceModel res = findResourceById(resourceId);
                if (task != null && res != null) {
                    // 双向关联
                    if (!task.getAssignedResources().contains(res)) {
                        task.getAssignedResources().add(res);
                    }
                    if (!res.getAssignedTasks().contains(task)) {
                        res.getAssignedTasks().add(task);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        // 触发界面刷新
        Platform.runLater(() -> {

            tasks.forEach(t -> t.getAssignedResources().size());
            resources.forEach(r -> r.getAssignedTasks().size());
        });
    }

    public ObservableList<TaskModel> getTasks() {
        return tasks;
    }

    public ObservableList<ResourceModel> getResources() {
        return resources;
    }


    public ResourceModel findResourceById(String id){
        return resources.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public TaskModel findTaskById(String id){
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    //自定义异常
    private static class DataLoadingException extends RuntimeException {
        public DataLoadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }




    public LocalDate getAnalysisStartDate() {
        return analysisStartDate;
    }

    public void setAnalysisStartDate(LocalDate analysisStartDate) {
        this.analysisStartDate = analysisStartDate;
    }

    public LocalDate getAnalysisEndDate() {
        return analysisEndDate;
    }

    public void setAnalysisEndDate(LocalDate analysisEndDate) {
        this.analysisEndDate = analysisEndDate;
    }
}