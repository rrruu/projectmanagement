
package com.example.projectmanagement.db;

import com.example.projectmanagement.model.ScheduleModel;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduleDAO {
    public static void insert(ScheduleModel schedule) throws SQLException {
        String sql = "INSERT INTO schedules(id, title, start_date, end_date, content) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, schedule.getTitle());
            pstmt.setString(3, schedule.getStartDate().toString());
            pstmt.setString(4, schedule.getEndDate().toString());
            pstmt.setString(5, schedule.getContent());
            pstmt.executeUpdate();
        }
    }

    public static List<ScheduleModel> getAll() throws SQLException {
        List<ScheduleModel> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ScheduleModel schedule = new ScheduleModel();
                schedule.setId(rs.getString("id"));
                schedule.setTitle(rs.getString("title"));
                schedule.setStartDate(LocalDate.parse(rs.getString("start_date")));
                schedule.setEndDate(LocalDate.parse(rs.getString("end_date")));
                schedule.setContent(rs.getString("content"));
                schedules.add(schedule);
            }
        }
        return schedules;
    }

    // 其他删除、更新方法省略
}