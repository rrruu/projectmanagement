package com.example.projectmanagement.controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainFrameController {
    private Stage primaryStage; // 添加成员变量

    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        loadGanttModule(); // 默认加载甘特图模块
    }


    // 新增设置primaryStage的方法
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void loadGanttModule() {
        loadModule("/com/example/projectmanagement/main.fxml");
    }

    @FXML
    private void loadResourceModule() {
        loadModule("/com/example/projectmanagement/resource_management.fxml");
    }

    @FXML
    private void loadScheduleModule() {
        loadModule("/com/example/projectmanagement/schedule.fxml");
    }





    private void loadModule(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll((Node) loader.load());

            // 传递primaryStage给需要它的控制器  如果加载的是甘特图模块，传递primaryStage参数
            if (fxmlPath.contains("main.fxml") && primaryStage != null) {
                MainController mainController = loader.getController();
                mainController.setPrimaryStage(primaryStage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}