package com.example.projectmanagement.controller;

import com.example.projectmanagement.Main;
import com.example.projectmanagement.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

/**
 * 主界面控制器
 */

public class GanttController {


    private Stage primaryStage;

    // 常量
    private static final double BASE_DAY_WIDTH = 40.0;  // 每天基础宽度
    private static final double ROW_HEIGHT = 30.0;     // 每行高度
    private static final double TIME_AXIS_HEIGHT = 80.0; // 时间轴总高度
    private static final double WEEK_SECTION_HEIGHT = 40.0; // 新增：周信息区域高度


    private boolean isWindowSizeListenerAdded = false; // 新增标记变量，避免绘制画布时重复注册监听器




    @FXML
    private TableView<TaskModel> taskTable;
    @FXML private TableColumn<TaskModel, String> taskNameColumn;
    @FXML private TableColumn<TaskModel, String> idColumn;
    @FXML private TableColumn<TaskModel, LocalDate> startDateColumn;
    @FXML private TableColumn<TaskModel, LocalDate> endDateColumn;
    @FXML private TableColumn<TaskModel, Number> durationColumn;
    @FXML private TableColumn<TaskModel, Number> progressColumn;
    @FXML private TableColumn<TaskModel, String> leaderColumn;
    @FXML private TableColumn<TaskModel, String> commentColumn;
    @FXML private TableColumn<TaskModel, Number> costColumn;
    @FXML private Canvas ganttCanvas;
    @FXML private ScrollPane ganttScrollPane;



    //    ObservableList<TaskModel>是关键数据容器，是JavaFX提供的可观察列表，特点如下
//    自动通知机制：当列表内容发生变动（增、删、改）时，会主动通知所有依赖它的 UI 组件。
//    与TableView绑定：通过 taskTable.setItems(tasks)，表格直接监听此列表的变动。


    private DataModel dataModel = DataModel.getInstance();
    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
        taskTable.setItems(dataModel.getTasks()); // 重新绑定数据

    }


    /**
     * 初始化方法，由FXML加载器自动调用
     */
    @FXML
    private void initialize() {



        // 初始化表格列
        taskNameColumn.setCellValueFactory(new PropertyValueFactory<>("taskName"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        leaderColumn.setCellValueFactory(new PropertyValueFactory<>("leader"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));


        //任务成本列
//        TableColumn<TaskModel, Number> costColumn = new TableColumn<>("任务成本");
        costColumn.setCellValueFactory(cellData -> cellData.getValue().costProperty());
//        taskTable.getColumns().add(costColumn);


        // 新增关联资源列
        TableColumn<TaskModel, String> resourcesColumn = new TableColumn<>("关联资源");
        resourcesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAssignedResourcesInfo())
        );
        //设置关联资源列宽
        resourcesColumn.setPrefWidth(200);
        taskTable.getColumns().add(resourcesColumn);


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



        taskTable.setItems(dataModel.getTasks());

        // 监听数据变化更新甘特图
        dataModel.getTasks().addListener((javafx.collections.ListChangeListener.Change<? extends TaskModel> c) -> {
            drawGanttChart();
        });


        drawGanttChart(); // 新增：初始化时立即绘制一次


    }


    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        // 监听窗口大小变化
        stage.widthProperty().addListener((obs, oldVal, newVal) -> drawGanttChart());
        stage.heightProperty().addListener((obs, oldVal, newVal) -> drawGanttChart());
    }


    @FXML
    private void handleAddTask() {
        try {
            // 加载新的FXML对话框
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/projectmanagement/addtask.fxml")
            );

            AnchorPane root = loader.load();
//            AddTaskController controller = loader.getController();

            // 创建并配置对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("添加新任务");
            dialogStage.initModality(Modality.APPLICATION_MODAL);//模态窗口
            dialogStage.initOwner(taskTable.getScene().getWindow());//设置父窗口



            //让addtask.fxml使用style.css的样式
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm());
            dialogStage.setScene(scene);



            // 显示窗口并等待
            dialogStage.showAndWait();


//            // 获取新任务（如果有）
//            TaskModel newTask = controller.getNewTask();
//            if (newTask != null) {
//                dataModel.getTasks().add(newTask);
//            }



            drawGanttChart();


        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "添加任务对话框加载失败：" + e.getMessage()).show();
        }
    }

    @FXML
    private void handleDeleteTask() {
        TaskModel selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择一个任务", ButtonType.OK).show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/projectmanagement/deletetask.fxml")
            );
            AnchorPane root = loader.load();
            DeleteTaskController controller = loader.getController();
            controller.setTaskToDelete(selectedTask);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("确认删除");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(taskTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            if (controller.isConfirmed()) {
                dataModel.getTasks().remove(selectedTask);
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "删除任务对话框加载失败：" + e.getMessage()).show();
        }
    }

    @FXML
    private void handleEditTask(){
        TaskModel selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择一个任务", ButtonType.OK).show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/projectmanagement/edittask.fxml")
            );

            AnchorPane root = loader.load();
            EditTaskController controller = loader.getController();
            controller.setTaskToEdit(selectedTask); // 传递待修改的任务

            Stage dialogStage = new Stage();
            dialogStage.setTitle("修改任务");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(taskTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // 由于 TaskModel 是对象引用，直接修改后无需额外操作
            // ObservableList 会自动通知 TableView 更新

            // 关键修改：仅在用户确认修改后触发重绘
            if (controller.isConfirmed()) {
                drawGanttChart();
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "修改任务对话框加载失败：" + e.getMessage()).show();
        }
    }







    @FXML
    private void handleLinkResources() {
        TaskModel selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择一个任务").show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/projectmanagement/link_resource.fxml")
            );
            AnchorPane root = loader.load();
            LinkResourceController controller = loader.getController();
            controller.setCurrentTask(selectedTask);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("关联资源");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            drawGanttChart(); // 刷新甘特图
        } catch (IOException e) {
            e.printStackTrace();
        }
    }










    @FXML
    private void handleExport(){
        try {
            // 确保画布已经绘制
            if (ganttCanvas == null || dataModel.getTasks().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "没有可导出的内容").show();
                return;
            }

            // 创建文件选择器
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存甘特图");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG 图片", "*.png"),
                    new FileChooser.ExtensionFilter("所有文件", "*.*")
            );

            // 设置默认文件名
            fileChooser.setInitialFileName("gantt_chart_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

            // 显示保存对话框
            File file = fileChooser.showSaveDialog(ganttCanvas.getScene().getWindow());
            if (file == null) return; // 用户取消操作

            // 获取画布快照并保存
            var image = ganttCanvas.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

            new Alert(Alert.AlertType.INFORMATION, "导出成功！\n保存路径：" + file.getAbsolutePath()).show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "导出失败：" + e.getMessage()).show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "发生未知错误").show();
        }
    }



    // 新增导入导出方法
    @FXML
    private void handleExportProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出项目文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON 文件", "*.json"));



        fileChooser.setInitialFileName("project_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

        File file = fileChooser.showSaveDialog(taskTable.getScene().getWindow());
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(TaskModel.class, new TaskModelTypeAdapter())
                    .registerTypeAdapter(ResourceModel.class, new ResourceModelTypeAdapter())
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .setPrettyPrinting()
                    .create();

            // 构建包含完整项目数据的JSON对象
            JsonObject project = new JsonObject();
            project.add("tasks", gson.toJsonTree(dataModel.getTasks()));
            project.add("resources", gson.toJsonTree(dataModel.getResources()));

            gson.toJson(project, writer);



//            // 直接序列化 ObservableList
//            gson.toJson(dataModel.getTasks(), writer);
            new Alert(Alert.AlertType.INFORMATION, "项目导出成功！").show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "导出失败：" + e.getMessage()).show();
        }
    }

    @FXML
    private void handleImportProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择项目文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON 文件", "*.json"));

        File file = fileChooser.showOpenDialog(taskTable.getScene().getWindow());
        if (file == null) return;

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(TaskModel.class, new TaskModelTypeAdapter()) // 添加TaskModel适配器
                    .registerTypeAdapter(ResourceModel.class, new ResourceModelTypeAdapter())
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .create();
////            // 直接反序列化为 ObservableList
////            Type taskListType = new TypeToken<ObservableList<TaskModel>>(){}.getType();
////            ObservableList<TaskModel> importedTasks = gson.fromJson(reader, taskListType);
//            // 改为反序列化为普通List
//            Type taskListType = new TypeToken<List<TaskModel>>(){}.getType();
//            List<TaskModel> importedTasks = gson.fromJson(reader, taskListType);
//
//
////            tasks.clear();
//
//            //使用 setAll 为直接导入（会清空原有内容）
//            dataModel.getTasks().setAll(importedTasks);

            // 解析完整项目数据
            JsonObject project = gson.fromJson(reader, JsonObject.class);

            // 先导入资源（因为任务需要引用资源）
            Type resourceListType = new TypeToken<List<ResourceModel>>(){}.getType();
            List<ResourceModel> importedResourcesList = gson.fromJson(
                    project.get("resources"),
                    resourceListType
            );
            ObservableList<ResourceModel> importedResources = FXCollections.observableArrayList(importedResourcesList);
            dataModel.getResources().setAll(importedResources);

            // 再导入任务（包含资源关联）
            Type taskListType = new TypeToken<List<TaskModel>>(){}.getType();
            List<TaskModel> importedTasksList = gson.fromJson(
                    project.get("tasks"),
                    taskListType
            );
            ObservableList<TaskModel> importedTasks = FXCollections.observableArrayList(importedTasksList);
            dataModel.getTasks().setAll(importedTasks);

            // 重建双向关联
            rebuildAssociations();

            drawGanttChart();
            new Alert(Alert.AlertType.INFORMATION, "项目导入成功！").show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "文件读取失败：" + e.getMessage()).show();
        } catch (JsonSyntaxException e) {
            new Alert(Alert.AlertType.ERROR, "文件格式错误").show();
        }
    }

    // 新增方法：重建资源与任务的关联关系
    private void rebuildAssociations() {
        // 清除所有现有关联
        dataModel.getResources().forEach(res -> res.getAssignedTasks().clear());
        dataModel.getTasks().forEach(task -> task.getAssignedResources().clear());

//        // 重建任务到资源的关联
//        for (TaskModel task : dataModel.getTasks()) {
//            for (ResourceModel res : task.getAssignedResources()) {
//                ResourceModel actualRes = dataModel.findResourceById(res.getId());
//                if (actualRes != null && !actualRes.getAssignedTasks().contains(task)) {
//                    actualRes.getAssignedTasks().add(task);
//                    task.getAssignedResources().add(actualRes);
//                }
//            }
//        }


        // 建立新关联
        dataModel.getTasks().forEach(task -> {
            task.getAssignedResources().replaceAll(tempRes -> {
                // 通过ID查找真实的资源对象
                ResourceModel realRes = dataModel.findResourceById(tempRes.getId());
                if (realRes != null) {
                    // 建立双向关联
                    realRes.getAssignedTasks().add(task);
                    return realRes;
                }
                return null; // 无效资源将被过滤
            });

            // 过滤掉无效的null值
            task.getAssignedResources().removeIf(Objects::isNull);
        });
    }


    @FXML
    private void handleAdditionalImportProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择项目文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON 文件", "*.json"));

        File file = fileChooser.showOpenDialog(taskTable.getScene().getWindow());
        if (file == null) return;

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(TaskModel.class, new TaskModelTypeAdapter()) // 添加TaskModel适配器
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .create();

            // 改为反序列化为普通List
            Type taskListType = new TypeToken<List<TaskModel>>(){}.getType();
            List<TaskModel> importedTasks = gson.fromJson(reader, taskListType);




            //使用 addAll 为追加导入，会保留原有内容
            dataModel.getTasks().addAll(importedTasks);
            drawGanttChart();
            new Alert(Alert.AlertType.INFORMATION, "项目导入成功！").show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "文件读取失败：" + e.getMessage()).show();
        } catch (JsonSyntaxException e) {
            new Alert(Alert.AlertType.ERROR, "文件格式错误").show();
        }
    }

    // 新增 LocalDate 序列化适配器
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString(), DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }




    public void drawGanttChart() {
        if (taskTable.getScene() == null || ganttCanvas == null) {
            return;
        }


        // 清空画布
        var gc = ganttCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, ganttCanvas.getWidth(), ganttCanvas.getHeight());
        if(dataModel.getTasks().isEmpty())return;


        // 计算调整后的时间范围（以周为单位）
        LocalDate minTaskStart = dataModel.getTasks().stream()
                .map(TaskModel::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate maxTaskEnd = dataModel.getTasks().stream()
                .map(TaskModel::getEndDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate adjustedProjectStart = minTaskStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate adjustedProjectEnd = maxTaskEnd.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));


        // 计算时间轴参数
        long totalDays = ChronoUnit.DAYS.between(adjustedProjectStart, adjustedProjectEnd) + 1;


        // 计算画布尺寸
        double canvasWidth = 100 + totalDays * BASE_DAY_WIDTH; // 左右边距各50
        double canvasHeight = TIME_AXIS_HEIGHT + dataModel.getTasks().size() * ROW_HEIGHT + 50;




        // 设置Canvas尺寸
        ganttCanvas.setWidth(canvasWidth);
        ganttCanvas.setHeight(canvasHeight);


        // 设置画布尺寸后，强制刷新 ScrollPane
        ganttScrollPane.setContent(ganttCanvas);
        ganttScrollPane.layout();



        // 绘制时间轴
        drawTimeAxis(gc, adjustedProjectStart, adjustedProjectEnd, canvasWidth);





        // 绘制任务条
        // 替换原有的硬编码颜色
        Color completedColor = getColorFromCss("gantt-completed");
        Color uncompletedColor = getColorFromCss("gantt-uncompleted");




        double yPos = TIME_AXIS_HEIGHT + 20; // 任务条起始Y坐标
        for (TaskModel task : dataModel.getTasks()) {


            long startOffset = ChronoUnit.DAYS.between(adjustedProjectStart, task.getStartDate());
            long duration = ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate()) + 1;
            double progress = task.getProgress();

            //任务起始位置
            double x = 50 + (startOffset * BASE_DAY_WIDTH);
            //任务总宽度
            double width = duration * BASE_DAY_WIDTH;
            //已完成部分宽度
            double finishedWidth = progress * width;
            //未完成部分宽度
            double unfinishedWidth = width - finishedWidth;





            if(finishedWidth > 0){
                gc.setFill(completedColor);
                gc.fillRect(x, yPos, finishedWidth, 20);
            }

            if(unfinishedWidth > 0){
                gc.setFill(uncompletedColor);
                gc.fillRect(x + finishedWidth, yPos, unfinishedWidth, 20);
            }




            // 绘制任务名称
            gc.setFill(Color.WHITE);
            gc.fillText(task.getTaskName(), x + 5, yPos + 15);

            yPos += ROW_HEIGHT;
        }





    }

    private void drawTimeAxis(javafx.scene.canvas.GraphicsContext gc, LocalDate start, LocalDate end, double canvasWidth){
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        //绘制时间轴上方周信息
        LocalDate currentMonday = start;
        int weekNumber = 1;
        while (!currentMonday.isAfter(end)){
            long daysFromStart = ChronoUnit.DAYS.between(start,currentMonday);
            double xPos = 50 + daysFromStart * BASE_DAY_WIDTH;

            // 绘制周分割线
            gc.strokeLine(xPos, 20, xPos, WEEK_SECTION_HEIGHT);

            // 显示周信息
            String weekInfo = "第" + weekNumber + "周 " + currentMonday.format(DateTimeFormatter.ofPattern("MM/dd"));
            gc.fillText(weekInfo, xPos + BASE_DAY_WIDTH/4, 35);//向右偏移1/4宽




            currentMonday = currentMonday.plusWeeks(1);
            weekNumber++;
        }


        // ========== 添加水平分割线 ==========
        gc.strokeLine(50, WEEK_SECTION_HEIGHT, canvasWidth - 50, WEEK_SECTION_HEIGHT);


        // 绘制下方日信息
        LocalDate currentDay = start;
        while (!currentDay.isAfter(end)) {
            long daysFromStart = ChronoUnit.DAYS.between(start, currentDay);
            double xPos = 50 + daysFromStart * BASE_DAY_WIDTH;

            // ========== 新增每日分割线 ==========
            gc.setStroke(Color.LIGHTGRAY); // 使用浅灰色区分
            gc.setLineWidth(0.5);          // 细线

            // 绘制从时间轴顶部到底部的分割线
            gc.strokeLine(xPos, 20, xPos, WEEK_SECTION_HEIGHT + 30);

            // 恢复默认样式
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);


            // 绘制日期
            // 日期文本（居中显示）
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


        // 绘制时间轴边框
        gc.setStroke(Color.BLACK);
        gc.strokeRect(50, 20, canvasWidth - 100, TIME_AXIS_HEIGHT - 30);
    }


    private Color getColorFromCss(String cssClass) {
        Pane tempNode = new Pane();
        String cssPath = Main.class.getResource("/com/example/projectmanagement/style.css").toExternalForm();

        tempNode.getStylesheets().add(cssPath);
        tempNode.getStyleClass().add(cssClass);


        // 必须将节点添加到场景图中才能应用样式
        new Scene(tempNode);
        tempNode.applyCss(); // 强制应用样式

        Background background = tempNode.getBackground();
        if (background != null && !background.getFills().isEmpty()) {
            Paint paint = background.getFills().get(0).getFill();
            if (paint instanceof Color) {
                return (Color) paint;
            }
        }
        return Color.RED; // 默认颜色 红色
    }





}