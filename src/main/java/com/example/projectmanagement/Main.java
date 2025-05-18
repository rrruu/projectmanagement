package com.example.projectmanagement;

import com.example.projectmanagement.controller.MainFrameController;
import com.example.projectmanagement.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 主应用类，负责启动JavaFX应用
 */

public class Main extends Application {


    @Override
    public void init() throws Exception {
        // 初始化数据库并清空项目相关表
        DatabaseManager.initialize();
        DatabaseManager.executeTransaction(() -> {
            try {
                DatabaseManager.clearProjectTables(); // 清空任务和资源表
            } catch (SQLException e) {
                throw new RuntimeException("初始化清空数据库失败", e);
            }
        });
    }


    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/example/projectmanagement/MainFrame.fxml")
        );
        Parent root = loader.load();


        // 获取主框架控制器并设置主舞台
        MainFrameController mainFrameController = loader.getController();
        mainFrameController.setPrimaryStage(primaryStage);


        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm());
        primaryStage.setTitle("Project Management Tool");
        primaryStage.setMaximized(true); // 窗口最大化
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // 关闭时清空项目相关表（保留日程表）
        DatabaseManager.executeTransaction(() -> {
            try {
                DatabaseManager.clearProjectTables();
            } catch (SQLException e) {
                throw new RuntimeException("关闭时清空数据库失败", e);
            }
        });
        DatabaseManager.close();
    }


    public static void main(String[] args) {
        launch();
    }
}
