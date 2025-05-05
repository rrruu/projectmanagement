package com.example.projectmanagement.controller;

import com.example.projectmanagement.Main;
import com.example.projectmanagement.model.ResourceModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ResourceManagementController {

    @FXML private TableView<ResourceModel> resourceTable;
    @FXML private TableColumn<ResourceModel,String> nameColumn;
    @FXML private TableColumn<ResourceModel,String> idColumn;
    @FXML private TableColumn<ResourceModel,String> phoneColumn;
    @FXML private TableColumn<ResourceModel,String> emailColumn;
    @FXML private TableColumn<ResourceModel,String> typeColumn;
    @FXML private TableColumn<ResourceModel,Number> rateColumn;


    private final ObservableList<ResourceModel> resources = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        //初始化表格绑定
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("dailyRate"));


        resourceTable.setItems(resources);
    }


    @FXML
    private void handleAddResource(){
        showResourceDialog(null);
    }

    @FXML
    private void handleDeleteResource(){

        ResourceModel selected = resourceTable.getSelectionModel().getSelectedItem();
        if(selected != null){
            resources.remove(selected);
        }
    }

    @FXML
    private void handleEditResource(){


        ResourceModel selected = resourceTable.getSelectionModel().getSelectedItem();
        if(selected != null){
            showResourceDialog(selected);
        }
    }



    private void showResourceDialog(ResourceModel resource){
        try {
            //加载对话框FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/projectmanagement/resource_dialog.fxml")
            );

            AnchorPane root = loader.load();
            ResourceDialogController controller = loader.getController();


            //初始化对话框
            Stage dialogStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm());
            dialogStage.setScene(scene);

            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
//            Dialog<ButtonType> dialog = new Dialog<>();
//            dialog.setDialogPane(root);
//            dialog.initModality(Modality.APPLICATION_MODAL);


            if(resource != null){
                controller.setResource(resource);
            }

            dialogStage.showAndWait();
            if(controller.isConfirmed()){
                ResourceModel newResource = controller.getResource();
                    if(resource == null){
                        resources.add(newResource);
                    }else {
                        resource.setName(newResource.getName());
                        resource.setId(newResource.getId());
                        resource.setPhone(newResource.getPhone());
                        resource.setEmail(newResource.getEmail());
                        resource.setType(newResource.getType());
                        resource.setDailyRate(newResource.getDailyRate());
                    }
            }

//        ifPresent(result -> {
//                if (result == ButtonType.OK){
//                    ResourceModel newResource = controller.getResource();
//                    if(resource == null){
//                        resources.add(newResource);
//                    }else {
//                        resource.setName(newResource.getName());
//                        resource.setId(newResource.getId());
//                        resource.setPhone(newResource.getPhone());
//                        resource.setEmail(newResource.getEmail());
//                        resource.setType(newResource.getType());
//                        resource.setDailyRate(newResource.getDailyRate());
//                    }
//                }
//            });


        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,"加载资源对话框失败").show();
        }
    }


    // 获取资源列表（后续用于绑定）
    public ObservableList<ResourceModel> getResources() {
        return resources;
    }







}