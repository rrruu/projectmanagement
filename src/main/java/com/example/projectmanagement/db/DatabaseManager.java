package com.example.projectmanagement.db;

import java.sql.*;

public class DatabaseManager {
    private static Connection connection;

    public static void initialize() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:project.db");
            connection.setAutoCommit(false); // 初始连接时关闭自动提交
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 任务表
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "start_date TEXT NOT NULL," +
                    "end_date TEXT NOT NULL," +
                    "progress REAL NOT NULL," +
                    "leader TEXT NOT NULL," +
                    "comment TEXT)");

            // 资源表
            stmt.execute("CREATE TABLE IF NOT EXISTS resources (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "phone TEXT," +
                    "email TEXT," +
                    "type TEXT NOT NULL," +
                    "daily_rate REAL NOT NULL," +
//                    "status TEXT," +
                    "comment TEXT)");

            // 任务资源关联表
            stmt.execute("CREATE TABLE IF NOT EXISTS task_resources (" +
                    "task_id TEXT NOT NULL," +
                    "resource_id TEXT NOT NULL," +
                    "PRIMARY KEY (task_id, resource_id)," +
                    "FOREIGN KEY (task_id) REFERENCES tasks(id)," +
                    "FOREIGN KEY (resource_id) REFERENCES resources(id))");


            //日程安排表
            stmt.execute("CREATE TABLE IF NOT EXISTS schedules (" +
                    "id TEXT PRIMARY KEY," +
                    "title TEXT NOT NULL," +
                    "start_date TEXT NOT NULL," +
                    "end_date TEXT NOT NULL," +
                    "content TEXT NOT NULL)");
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void executeTransaction(Runnable operation) {
        Connection conn = getConnection();
        try {
            boolean originalAutoCommit = conn.getAutoCommit();//记录原始状态
            conn.setAutoCommit(false);//开启事务
            operation.run();//执行传入的业务逻辑
            conn.commit();  //提交事务
            conn.setAutoCommit(originalAutoCommit); // 恢复auto-commit原始状态
        } catch (Exception e) {
            try {
                conn.rollback();//如果出错，回滚事务
            } catch (SQLException ex) {
                ex.printStackTrace();//打印回滚失败日志
            }
            throw new RuntimeException("Transaction failed", e);//抛出原始异常，供外部捕获或记录
        }


    }


}
