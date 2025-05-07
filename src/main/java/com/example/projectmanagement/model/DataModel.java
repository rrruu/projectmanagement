// DataModel.java
package com.example.projectmanagement.model;

import com.example.projectmanagement.db.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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
        try(PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM tasks"
        )){
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                TaskModel task = new TaskModel(
                        rs.getString("name"),
                        rs.getString("id"),
                        LocalDate.parse(rs.getString("start_date")),
                        LocalDate.parse(rs.getString("end_date")),
                        rs.getDouble("progress"),
                        rs.getString("leader"),
                        rs.getString("comment")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 加载资源
    public void loadResources() {
        resources.clear();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM resources")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ResourceModel res = new ResourceModel(
                        rs.getString("name"),
                        rs.getString("id"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("type"),
                        rs.getDouble("daily_rate"),
                        rs.getString("comment")
                );
                resources.add(res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 加载关联关系
    private void loadAssociations() {
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
}