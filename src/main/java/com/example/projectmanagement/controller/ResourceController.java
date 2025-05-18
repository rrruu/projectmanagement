package com.example.projectmanagement.controller;

import com.example.projectmanagement.Main;
import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceController {



    // 甘特图常量
    private static final double BASE_DAY_WIDTH = 40.0;
    private static final double ROW_HEIGHT = 30.0;
    private static final double TIME_AXIS_HEIGHT = 80.0;
    private static final double WEEK_SECTION_HEIGHT = 40.0;

    @FXML private TableView<ResourceModel> resourceTable;
    @FXML private TableColumn<ResourceModel,String> nameColumn;
    @FXML private TableColumn<ResourceModel,String> idColumn;
    @FXML private TableColumn<ResourceModel,String> phoneColumn;
    @FXML private TableColumn<ResourceModel,String> emailColumn;
    @FXML private TableColumn<ResourceModel,String> typeColumn;
    @FXML private TableColumn<ResourceModel,Number> rateColumn;
    @FXML private TableColumn<ResourceModel,String> commentColumn;
//    @FXML private TableColumn<ResourceModel, String> statusColumn;
    @FXML private Canvas resourceGanttCanvas;
    @FXML private ScrollPane resourceGanttScrollPane;


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
//        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));





        //关联任务列
        TableColumn<ResourceModel, String> tasksColumn = new TableColumn<>("关联任务");
        tasksColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAssignedTasksInfo())
        );
        //设置关联任务列宽
        tasksColumn.setPrefWidth(200);
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


        // 新增数据监听
        dataModel.getResources().addListener((ListChangeListener<? super ResourceModel>) (change) -> drawResourceGantt());
        dataModel.getTasks().addListener((ListChangeListener<? super TaskModel>) (change) -> drawResourceGantt());
        drawResourceGantt();
    }


    @FXML
    private void handleAddResource(){
        try {
            //加载新的FXML对话框
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/projectmanagement/resourceadd.fxml")
            );
            AnchorPane root = loader.load();
//            ResourceAddController controller = loader.getController();
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












    @FXML
    private void handleLinkTasks() {
        ResourceModel selected = resourceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择一个资源").show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/projectmanagement/link_task.fxml")
            );
            AnchorPane root = loader.load();
            LinkTaskController controller = loader.getController();
            controller.setCurrentResource(selected);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("关联任务");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            resourceTable.refresh(); // 刷新资源列表
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    // 获取资源列表
    public ObservableList<ResourceModel> getResources() {
        return dataModel.getResources();
    }



    private void drawResourceGantt() {
        if (resourceGanttCanvas == null || dataModel.getResources().isEmpty()) return;

        GraphicsContext gc = resourceGanttCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, resourceGanttCanvas.getWidth(), resourceGanttCanvas.getHeight());



        // 过滤无效任务（确保日期不为空）
        List<TaskModel> validTasks = dataModel.getResources().stream()
                .flatMap(res -> res.getAssignedTasks().stream())
                .filter(task -> task.getStartDate() != null && task.getEndDate() != null)
                .collect(Collectors.toList());

        if (validTasks.isEmpty()) {
            resourceGanttCanvas.getGraphicsContext2D().clearRect(0, 0, resourceGanttCanvas.getWidth(), resourceGanttCanvas.getHeight());
            return;
        }


        // 计算时间范围（基于有效任务）
        LocalDate minDate = validTasks.stream()
                .map(TaskModel::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate maxDate = validTasks.stream()
                .map(TaskModel::getEndDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());



        // 调整到完整周
        LocalDate adjustedStart = minDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate adjustedEnd = maxDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 计算画布尺寸
        long totalDays = ChronoUnit.DAYS.between(adjustedStart, adjustedEnd) + 1;
        double canvasWidth = 100 + totalDays * BASE_DAY_WIDTH;
        double canvasHeight = TIME_AXIS_HEIGHT + dataModel.getResources().size() * ROW_HEIGHT + 50;

        resourceGanttCanvas.setWidth(canvasWidth);
        resourceGanttCanvas.setHeight(canvasHeight);
        resourceGanttScrollPane.layout();

        // 绘制时间轴
        drawResourceTimeAxis(gc, adjustedStart, adjustedEnd, canvasWidth);

        // 绘制资源任务条
        double yPos = TIME_AXIS_HEIGHT + 20;
        for (ResourceModel resource : dataModel.getResources()) {
            for (TaskModel task : resource.getAssignedTasks()) {
                long startOffset = ChronoUnit.DAYS.between(adjustedStart, task.getStartDate());
                long duration = ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate()) + 1;

                double x = 50 + startOffset * BASE_DAY_WIDTH;
                double width = duration * BASE_DAY_WIDTH;

                gc.setFill(Color.rgb(111, 146, 175)); // 钢蓝色
                gc.fillRect(x, yPos, width, 20);

                //绘制任务名称
                gc.setFill(Color.WHITE);
                gc.fillText(task.getTaskName(), x + 5, yPos + 15);
            }
            yPos += ROW_HEIGHT;
        }
    }

    private void drawResourceTimeAxis(GraphicsContext gc, LocalDate start, LocalDate end, double canvasWidth) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        // 绘制周信息
        LocalDate currentMonday = start;
        int weekNumber = 1;
        while (!currentMonday.isAfter(end)) {
            long daysFromStart = ChronoUnit.DAYS.between(start, currentMonday);
            double xPos = 50 + daysFromStart * BASE_DAY_WIDTH;

            // 周分割线
            gc.strokeLine(xPos, 20, xPos, WEEK_SECTION_HEIGHT);

            // 周标签
            String weekInfo = "第" + weekNumber + "周 " + currentMonday.format(DateTimeFormatter.ofPattern("MM/dd"));
            gc.fillText(weekInfo, xPos + BASE_DAY_WIDTH/4, 35);

            currentMonday = currentMonday.plusWeeks(1);
            weekNumber++;
        }

        // ========== 添加水平分割线 ==========
        gc.strokeLine(50, WEEK_SECTION_HEIGHT, canvasWidth - 50, WEEK_SECTION_HEIGHT);


        // 绘制日期轴
        LocalDate currentDay = start;
        while (!currentDay.isAfter(end)) {
            long daysFromStart = ChronoUnit.DAYS.between(start, currentDay);
            double xPos = 50 + daysFromStart * BASE_DAY_WIDTH;

            // 每日分割线
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(0.5);

            // 绘制从时间轴顶部到底部的分割线
            gc.strokeLine(xPos, 20, xPos, WEEK_SECTION_HEIGHT + 30);

            // 恢复默认样式
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);


            // 日期标签
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(currentDay.getDayOfMonth()),
                    xPos + BASE_DAY_WIDTH/2,
                    WEEK_SECTION_HEIGHT + 25);
            gc.setTextAlign(TextAlignment.LEFT);
            // 每周日绘制分隔线（延长到分割线下方）
            if (currentDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
                gc.strokeLine(xPos + BASE_DAY_WIDTH,
                        WEEK_SECTION_HEIGHT,//从分割线开始
                        xPos + BASE_DAY_WIDTH,
                        WEEK_SECTION_HEIGHT + 30
                );
            }
            currentDay = currentDay.plusDays(1);
        }

        // 边框
        gc.setStroke(Color.BLACK);
        gc.strokeRect(50, 20, canvasWidth - 100, TIME_AXIS_HEIGHT - 30);
    }




}