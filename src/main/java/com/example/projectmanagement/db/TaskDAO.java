package com.example.projectmanagement.db;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


public class TaskDAO {
    public static void create(TaskModel task) throws SQLException {
        String sql = "INSERT INTO tasks(id, name, start_date, end_date, progress, leader, comment) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            bindTaskParametersForCreate(stmt, task);
            stmt.executeUpdate();
        }
    }

    public static void update(TaskModel task) throws SQLException {
        String sql = "UPDATE tasks SET name=?, start_date=?, end_date=?, progress=?, leader=?, comment=? "
                + "WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            bindTaskParametersForUpdate(stmt, task);
            stmt.executeUpdate();
        }
    }

    private static void bindTaskParametersForCreate(PreparedStatement stmt, TaskModel task) throws SQLException {

        //id在update方法中单独设置
        stmt.setString(1, task.getId());
        stmt.setString(2, task.getTaskName());
        stmt.setString(3, task.getStartDate().toString());
        stmt.setString(4, task.getEndDate().toString());
        stmt.setDouble(5, task.getProgress());
        stmt.setString(6, task.getLeader());
        stmt.setString(7, task.getComment());
    }

    private static void bindTaskParametersForUpdate(PreparedStatement stmt, TaskModel task) throws SQLException {


        stmt.setString(1, task.getTaskName());
        stmt.setString(2, task.getStartDate().toString());
        stmt.setString(3, task.getEndDate().toString());
        stmt.setDouble(4, task.getProgress());
        stmt.setString(5, task.getLeader());
        stmt.setString(6, task.getComment());
        stmt.setString(7, task.getId());
    }

    public static List<TaskModel> findAll() throws SQLException {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }
        return tasks;
    }

    private static TaskModel mapResultSetToTask(ResultSet rs) throws SQLException {
        try {
            return new TaskModel(
                    rs.getString("name"),
                    rs.getString("id"),
                    LocalDate.parse(rs.getString("start_date")),
                    LocalDate.parse(rs.getString("end_date")),
                    rs.getDouble("progress"),
                    rs.getString("leader"),
                    rs.getString("comment")
            );
        } catch (DateTimeParseException e) {
            throw new SQLException("Invalid date format in database", e);
        }
    }







    public static void delete(String taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, taskId);
            stmt.executeUpdate();
        }
    }

    public static void clearTaskResources(String taskId) throws SQLException {
        String sql = "DELETE FROM task_resources WHERE task_id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, taskId);
            stmt.executeUpdate();
        }
    }

    public static void addTaskResources(String taskId, List<ResourceModel> resources) throws SQLException {
        String sql = "INSERT INTO task_resources(task_id, resource_id) VALUES(?,?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            for (ResourceModel res : resources) {
                stmt.setString(1, taskId);
                stmt.setString(2, res.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }







}
