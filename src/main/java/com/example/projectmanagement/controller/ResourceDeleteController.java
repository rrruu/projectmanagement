package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.ResourceModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ResourceDeleteController {


    @FXML
    private Label messageLabel;
    private ResourceModel resourceToDelete;
    private boolean isconfirmed = false;


    public void setResourceToDelete(ResourceModel resource){
        this.resourceToDelete = resource;
        messageLabel.setText("确定要删除资源 '" + resource.getId() + "' 吗？");
    }




    @FXML
    private void handleConfirm(){
        isconfirmed = true;
        messageLabel.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel(){
        isconfirmed = false;
        messageLabel.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return isconfirmed;
    }
}
