package com.example.projectmanagement.controller;


import com.example.projectmanagement.model.DataModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Optional;

public class MainFrameController {
    private Stage primaryStage; // 添加成员变量

    @FXML
    private StackPane contentPane;


    // 注入按钮
    @FXML private Button taskButton;
    @FXML private Button resourceButton;
    @FXML private Button resourceAnalysisButton;
    @FXML private Button scheduleButton;

    private Button selectedButton; // 当前选中的按钮

    @FXML
    private void initialize() {
        loadWelcomeModule(); // 默认加载欢迎界面
    }


    // 新增设置primaryStage的方法
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupCloseHandler(); // 设置关闭事件处理器
    }


    // 新增：关闭事件处理逻辑
    private void setupCloseHandler() {
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("关闭提示");
            alert.setHeaderText("正在关闭，请注意保存项目文件！");
            alert.setContentText("是否确定退出？");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 清空数据模型
                DataModel.getInstance().getTasks().clear();
                DataModel.getInstance().getResources().clear();
            } else {
                event.consume(); // 取消关闭操作
            }
        });
    }

    @FXML
    private void loadGanttModule() {
        updateSelectedButton(taskButton);
        loadModule("/com/example/projectmanagement/task.fxml");
    }

    @FXML
    private void loadResourceModule() {
        updateSelectedButton(resourceButton);
        loadModule("/com/example/projectmanagement/resource.fxml");
    }

    @FXML
    private void loadResourceAnalysisModule() {
        updateSelectedButton(resourceAnalysisButton);
        loadModule("/com/example/projectmanagement/resourceanalysis.fxml");
    }

    @FXML
    private void loadScheduleModule() {
        updateSelectedButton(scheduleButton);
        loadModule("/com/example/projectmanagement/schedule.fxml");
    }

    @FXML
    private void loadWelcomeModule() {

        loadModule("/com/example/projectmanagement/welcome.fxml");
    }





    private void loadModule(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            Node node = loader.load();


            //如果是甘特图模块，传递DataModel
            if(fxmlPath.contains("task.fxml")){
                TaskController controller = loader.getController();
                controller.setDataModel(DataModel.getInstance());
                Platform.runLater(() -> {
                    controller.drawGanttChart();
                });
            }

            //如果是资源管理模块，传递DataModel
            if(fxmlPath.contains("resource.fxml")){
                ResourceController controller = loader.getController();
                controller.setDataModel(DataModel.getInstance());

            }


            contentPane.getChildren().setAll(node);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 更新选中按钮的样式
    private void updateSelectedButton(Button newButton) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("navButtonMain-selected");
            selectedButton.getStyleClass().add("navButtonMain");
        }
        selectedButton = newButton;
        selectedButton.getStyleClass().remove("navButtonMain");
        selectedButton.getStyleClass().add("navButtonMain-selected");
    }
}