module com.example.projectmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;
    requires java.logging;
    requires java.xml;
    requires java.naming;
    requires java.transaction.xa;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires org.xerial.sqlitejdbc;

    opens com.example.projectmanagement to javafx.fxml;
    opens com.example.projectmanagement.controller to javafx.fxml;

    exports com.example.projectmanagement;
    exports com.example.projectmanagement.controller;

    // 关键配置：允许 javafx.fxml 模块访问 controller 包
    opens com.example.projectmanagement.controller to javafx.fxml;

    exports com.example.projectmanagement.controller; // 导出控制器包（可选）

    opens com.example.projectmanagement.model to com.google.gson, javafx.base, javafx.fxml;

    exports com.example.projectmanagement.model; // 新增这行
}