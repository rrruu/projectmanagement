package com.example.projectmanagement;

import com.example.projectmanagement.controller.MainFrameController;
import com.example.projectmanagement.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 主应用类，负责启动JavaFX应用
 */

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // 初始化数据库
        DatabaseManager.initialize();
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/example/projectmanagement/MainFrame.fxml")
        );
        Parent root = loader.load();


        // 获取主框架控制器并设置主舞台
        MainFrameController mainFrameController = loader.getController();
        mainFrameController.setPrimaryStage(primaryStage);


        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm());
        primaryStage.setTitle("项目管理系统");
        primaryStage.setMaximized(true); // 窗口最大化
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.close();
    }


    public static void main(String[] args) {
        launch();
    }
}
