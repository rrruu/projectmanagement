package com.example.projectmanagement.controller;

import com.example.projectmanagement.db.DatabaseManager;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResourceDeleteController {


    @FXML
    public Label messageLabel;
    public ResourceModel resourceToDelete;
    public boolean isconfirmed = false;


    public void setResourceToDelete(ResourceModel resource){
        this.resourceToDelete = resource;
        messageLabel.setText("确定要删除资源 '" + resource.getId() + "' 吗？");
    }




    @FXML
    private void handleConfirm(){

        try {
            deleteResourceFromDatabase(resourceToDelete);
            deleteResourceAssociations(resourceToDelete);
            DatabaseManager.getConnection().commit();//提交事务
            DataModel.getInstance().loadAllData();//重新加载数据
            isconfirmed = true;
            messageLabel.getScene().getWindow().hide();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "删除失败").show();
            rollbackTransaction();
        }

    }

    @FXML
    private void handleCancel(){
        isconfirmed = false;
        messageLabel.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return isconfirmed;
    }


    private void deleteResourceFromDatabase(ResourceModel resource) throws SQLException{
        String sql = "DELETE FROM resources WHERE id=?";
        try(PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1,resource.getId());
            stmt.executeUpdate();
        }
    }



    private void deleteResourceAssociations(ResourceModel resource) throws SQLException {
        String sql = "DELETE FROM task_resources WHERE resource_id=?";
        try(PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1,resource.getId());
            stmt.executeUpdate();
        }
    }

    private void showErrorAlert(Exception e) {
        new Alert(Alert.AlertType.ERROR,
                "操作失败：" + e.getMessage(),
                ButtonType.OK).show();
    }

    private void rollbackTransaction() {
        try {
            DatabaseManager.getConnection().rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
