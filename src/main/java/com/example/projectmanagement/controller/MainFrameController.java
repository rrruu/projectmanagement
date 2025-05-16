package com.example.projectmanagement.controller;


import com.example.projectmanagement.model.DataModel;
import javafx.application.Platform;
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
        loadWelcomeModule(); // 默认加载欢迎界面
    }


    // 新增设置primaryStage的方法
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void loadGanttModule() {
        loadModule("/com/example/projectmanagement/gantt.fxml");
    }

    @FXML
    private void loadResourceModule() {
        loadModule("/com/example/projectmanagement/resource_management.fxml");
    }

    @FXML
    private void loadResourceAnalysisModule() { loadModule("/com/example/projectmanagement/resourceanalysis.fxml");}

    @FXML
    private void loadScheduleModule() {
        loadModule("/com/example/projectmanagement/schedule.fxml");
    }

    @FXML
    private void loadWelcomeModule() { loadModule("/com/example/projectmanagement/welcome.fxml"); }





    private void loadModule(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            Node node = loader.load();


            //如果是甘特图模块，传递DataModel
            if(fxmlPath.contains("gantt.fxml")){
                GanttController controller = loader.getController();
                controller.setDataModel(DataModel.getInstance());
                Platform.runLater(() -> {
                    controller.drawGanttChart();
                });
            }

            //如果是资源管理模块，传递DataModel
            if(fxmlPath.contains("resource_management.fxml")){
                ResourceManagementController controller = loader.getController();
                controller.setDataModel(DataModel.getInstance());

            }


            contentPane.getChildren().setAll(node);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}