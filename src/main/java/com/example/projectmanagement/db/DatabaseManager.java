package com.example.projectmanagement.db;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static Connection connection;
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_FILE = System.getProperty("user.home") + File.separator + ".projectmanagement"
            + File.separator + "project.db";

    public static void initialize() {
        try {
            // 确保数据库目录存在
            File dbDir = new File(DB_FILE).getParentFile();
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            Class.forName("org.sqlite.JDBC"); // 显式加载驱动
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            connection.setAutoCommit(false); // 初始连接时关闭自动提交
            createTables();
            logger.info("Database initialized successfully at: " + DB_FILE);
        } catch (SQLException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
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
                    "comment TEXT)");

            // 任务资源关联表
            stmt.execute("CREATE TABLE IF NOT EXISTS task_resources (" +
                    "task_id TEXT NOT NULL," +
                    "resource_id TEXT NOT NULL," +
                    "PRIMARY KEY (task_id, resource_id)," +
                    "FOREIGN KEY (task_id) REFERENCES tasks(id)," +
                    "FOREIGN KEY (resource_id) REFERENCES resources(id))");

            // 日程安排表
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
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    public static void executeTransaction(Runnable operation) {
        Connection conn = getConnection();
        try {
            boolean originalAutoCommit = conn.getAutoCommit();// 记录原始状态
            conn.setAutoCommit(false);// 开启事务
            operation.run();// 执行传入的业务逻辑
            conn.commit(); // 提交事务
            conn.setAutoCommit(originalAutoCommit); // 恢复auto-commit原始状态
        } catch (Exception e) {
            try {
                System.err.println("事务回滚，原因：" + e.getMessage());
                conn.rollback();// 如果出错，回滚事务
            } catch (SQLException ex) {
                System.err.println("回滚失败：" + ex.getMessage());
            }
            throw new RuntimeException("Transaction failed", e);// 抛出原始异常，供外部捕获或记录
        }

    }

    // 清理任务和资源相关表（不清理日程表）
    public static void clearProjectTables() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("DELETE FROM task_resources");
            stmt.executeUpdate("DELETE FROM tasks");
            stmt.executeUpdate("DELETE FROM resources");
        }
    }

}
