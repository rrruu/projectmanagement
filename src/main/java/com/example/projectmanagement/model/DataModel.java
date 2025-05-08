// DataModel.java
package com.example.projectmanagement.model;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.db.ResourceDAO;
import com.example.projectmanagement.db.TaskDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DataModel {
    private static DataModel instance = new DataModel();

    private ObservableList<TaskModel> tasks = FXCollections.observableArrayList();
    private ObservableList<ResourceModel> resources = FXCollections.observableArrayList();

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
            tasks.setAll(newTasks);
            loadAssociations(); // 加载关联关系
        } catch (SQLException e) {
            throw new DataLoadingException("Failed to load tasks", e);
        }
    }

    // 加载资源
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

    // 加载关联关系
    public void loadAssociations() {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM task_resources")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskModel task = findTaskById(rs.getString("task_id"));
                ResourceModel res = findResourceById(rs.getString("resource_id"));
                if (task != null && res != null) {
                    task.getAssignedResources().add(res);
                    res.getAssignedTasks().add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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


    // 新增自定义异常
    private static class DataLoadingException extends RuntimeException {
        public DataLoadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}