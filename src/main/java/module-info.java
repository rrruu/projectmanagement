module com.example.projectmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires com.google.gson;
    requires java.sql;


    opens com.example.projectmanagement to javafx.fxml,javafx.graphics;
    exports com.example.projectmanagement;

    // 关键配置：允许 javafx.fxml 模块访问 controller 包
    opens com.example.projectmanagement.controller to javafx.fxml;
    exports com.example.projectmanagement.controller; // 导出控制器包（可选）

    opens com.example.projectmanagement.model to com.google.gson, javafx.base, javafx.fxml;
    exports com.example.projectmanagement.model; // 新增这行
}