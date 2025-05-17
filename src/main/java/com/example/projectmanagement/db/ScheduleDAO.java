
package com.example.projectmanagement.db;

import com.example.projectmanagement.model.ScheduleModel;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduleDAO {
    public static void create(ScheduleModel schedule) throws SQLException {
        String sql = "INSERT INTO schedules(id, title, start_date, end_date, content) VALUES(?,?,?,?,?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, schedule.getId());
            stmt.setString(2, schedule.getTitle());
            stmt.setString(3, schedule.getStartDate().toString());
            stmt.setString(4, schedule.getEndDate().toString());
            stmt.setString(5, schedule.getContent());
            stmt.executeUpdate();
        }
    }

    public static List<ScheduleModel> findAll() throws SQLException {
        List<ScheduleModel> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                schedules.add(new ScheduleModel(
                        rs.getString("id"),
                        rs.getString("title"),
                        LocalDate.parse(rs.getString("start_date")),
                        LocalDate.parse(rs.getString("end_date")),
                        rs.getString("content")
                ));
            }
        }
        return schedules;
    }

    public static void delete(String id) throws SQLException {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }
}