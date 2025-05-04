package com.example.projectmanagement;

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
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/example/projectmanagement/main.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root,1100,600);
        scene.getStylesheets().add(Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm());
        stage.setTitle("甘特图生成");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}
