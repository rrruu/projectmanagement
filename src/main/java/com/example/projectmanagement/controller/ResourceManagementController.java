package com.example.projectmanagement.controller;

import com.example.projectmanagement.Main;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import javafx.beans.property.SimpleStringProperty;
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
    @FXML private TableColumn<ResourceModel,String> commentColumn;
    @FXML private TableColumn<ResourceModel, String> statusColumn;


//    private final ObservableList<ResourceModel> resources = FXCollections.observableArrayList();
    private DataModel dataModel = DataModel.getInstance();
    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
        resourceTable.setItems(dataModel.getResources()); // 重新绑定数据
    }






    @FXML
    private void initialize(){
        //初始化表格绑定
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("dailyRate"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));





        //新增关联任务列
        TableColumn<ResourceModel, String> tasksColumn = new TableColumn<>("关联任务");
        tasksColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAssignedTasksInfo())
        );
        resourceTable.getColumns().add(tasksColumn);




        //为备注列定义自定义的单元格渲染逻辑
        // 为备注列添加Tooltip
        commentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                //item：当前单元格绑定的数据（TaskModel的comment属性值）
                //empty：标识单元格是否为空（无数据）
                super.updateItem(item, empty);
                if (item == null || empty) {
                    //如果数据为空或单元格无内容，清空文本和Tooltip
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(abbreviateString(item, 15)); // 将备注内容截断为前15个字符，超过15字符添加省略号
                    Tooltip tooltip = new Tooltip(item); //用完整备注内容创建提示
                    tooltip.setWrapText(true);//允许文本自动换行
                    tooltip.setMaxWidth(400);//限制提示框最大宽度为400像素，避免过宽
                    setTooltip(tooltip);//鼠标悬停时显示完整备注
                }
            }

            // 字符串截断方法
            private String abbreviateString(String str, int maxLength) {
                return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
            }
        });

















        resourceTable.setItems(dataModel.getResources());
    }


    @FXML
    private void handleAddResource(){
        try {
            //加载新的FXML对话框
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/projectmanagement/resourceadd.fxml")
            );
            AnchorPane root = loader.load();
            ResourceAddController controller = loader.getController();
            //创建并配置对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("添加新资源");
            dialogStage.initModality(Modality.APPLICATION_MODAL);//模态窗口
            dialogStage.initOwner(resourceTable.getScene().getWindow());//设置父窗口


            //让resourceadd.fxml使用style.css的样式
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm());
            dialogStage.setScene(scene);


            // 显示窗口并等待
            dialogStage.showAndWait();


            //获取新资源（如果有）
            ResourceModel newResource = controller.getNewResource();
            if(newResource != null){
                dataModel.getResources().add(newResource);
            }


        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "添加资源对话框加载失败：" + e.getMessage()).show();
        }

    }

    @FXML
    private void handleDeleteResource(){

        ResourceModel selectedResource = resourceTable.getSelectionModel().getSelectedItem();
        if(selectedResource == null){
            new Alert(Alert.AlertType.WARNING, "请先选择一个资源", ButtonType.OK).show();
            return;
        }


        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/projectmanagement/resourcedelete.fxml")
            );

            AnchorPane root = loader.load();
            ResourceDeleteController controller = loader.getController();
            controller.setResourceToDelete(selectedResource);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("确认删除");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(resourceTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            if(controller.isConfirmed()){
                dataModel.getResources().remove(selectedResource);
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "删除资源对话框加载失败：" + e.getMessage()).show();
        }
    }

    @FXML
    private void handleEditResource(){


        ResourceModel selectedResource = resourceTable.getSelectionModel().getSelectedItem();
        if(selectedResource == null){
            new Alert(Alert.AlertType.WARNING, "请先选择一个资源", ButtonType.OK).show();
            return;
        }


        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/projectmanagement/resourceedit.fxml")
            );

            AnchorPane root = loader.load();
            ResourceEditController controller = loader.getController();
            controller.setResourceToEdit(selectedResource);// 传递待修改的任务

            Stage dialogStage = new Stage();
            dialogStage.setTitle("修改资源");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(resourceTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // 由于 ResourceModel 是对象引用，直接修改后无需额外操作
            // ObservableList 会自动通知 ResourceView 更新




        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "修改资源对话框加载失败：" + e.getMessage()).show();
        }


    }



//    private void showResourceDialog(ResourceModel resource){
//        try {
//            //加载对话框FXML
//            FXMLLoader loader = new FXMLLoader(
//                    getClass().getResource("/com/example/projectmanagement/resourceadd.fxml")
//            );
//
//            AnchorPane root = loader.load();
//            ResourceAddController controller = loader.getController();
//
//
//            //初始化对话框
//            Stage dialogStage = new Stage();
//            Scene scene = new Scene(root);
//            scene.getStylesheets().add(Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm());
//            dialogStage.setScene(scene);
//
//            dialogStage.setScene(scene);
//            dialogStage.initModality(Modality.APPLICATION_MODAL);
////            Dialog<ButtonType> dialog = new Dialog<>();
////            dialog.setDialogPane(root);
////            dialog.initModality(Modality.APPLICATION_MODAL);
//
//
//            if(resource != null){
//                controller.setResource(resource);
//            }
//
//            dialogStage.showAndWait();
//            if(controller.isConfirmed()){
//                ResourceModel newResource = controller.getResource();
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
//            }
//
////        ifPresent(result -> {
////                if (result == ButtonType.OK){
////                    ResourceModel newResource = controller.getResource();
////                    if(resource == null){
////                        resources.add(newResource);
////                    }else {
////                        resource.setName(newResource.getName());
////                        resource.setId(newResource.getId());
////                        resource.setPhone(newResource.getPhone());
////                        resource.setEmail(newResource.getEmail());
////                        resource.setType(newResource.getType());
////                        resource.setDailyRate(newResource.getDailyRate());
////                    }
////                }
////            });
//
//
//        } catch (IOException e) {
//            new Alert(Alert.AlertType.ERROR,"加载资源对话框失败").show();
//        }
//    }


    // 获取资源列表（后续用于绑定）
    public ObservableList<ResourceModel> getResources() {
        return dataModel.getResources();
    }







}