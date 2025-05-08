package com.example.projectmanagement.db;

import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    public static void create(ResourceModel resource) throws SQLException {
        String sql = "INSERT INTO resources(id, name, phone, email, type, daily_rate, status, comment) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            bindResourceParameters(stmt, resource);
            stmt.executeUpdate();
        }
    }


    public static void update(ResourceModel resource) throws SQLException {
        String sql = "UPDATE resources SET name=?, phone=?, email=?, type=?, daily_rate=?, status=?, comment=? "
                + "WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            bindResourceParameters(stmt, resource);
            stmt.setString(8, resource.getId());
            stmt.executeUpdate();
        }
    }

    private static void bindResourceParameters(PreparedStatement stmt, ResourceModel resource) throws SQLException {

        stmt.setString(1, resource.getId());
        stmt.setString(2, resource.getName());
        stmt.setString(3, resource.getPhone());
        stmt.setString(4, resource.getEmail());
        stmt.setString(5, resource.getType());
        stmt.setDouble(6, resource.getDailyRate());
        stmt.setString(7, resource.getStatus());
        stmt.setString(8, resource.getComment());

    }

    public static List<ResourceModel> findAll() throws SQLException {
        List<ResourceModel> resources = new ArrayList<>();
        String sql = "SELECT * FROM resources";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resources.add(mapResultSetToResource(rs));
            }
        }
        return resources;
    }

    private static ResourceModel mapResultSetToResource(ResultSet rs) throws SQLException {
        return new ResourceModel(
                rs.getString("name"),
                rs.getString("id"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("type"),
                rs.getDouble("daily_rate"),
                rs.getString("comment")
        );
    }



}
