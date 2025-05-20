package com.example.projectmanagement.controller;


import com.example.projectmanagement.db.ScheduleDAO;
import com.example.projectmanagement.model.ScheduleModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScheduleController {


    @FXML
    private Label monthLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML private FlowPane cardsContainer;

    // 新增甘特图相关变量
    @FXML private Canvas scheduleGanttCanvas;
    @FXML private ScrollPane scheduleGanttScrollPane;

    @FXML private MenuButton filterMenuButton;

    private ObservableList<ScheduleModel> schedules = FXCollections.observableArrayList();

    private YearMonth currentYearMonth;

    private boolean isRefreshing = false; // 添加标志位


    // 新增成员变量：当前选中的日程
    private ScheduleModel selectedSchedule;

    // 新增成员变量：是否显示本周日程的标志
    private boolean showThisWeek = false;

    // 新增枚举类型定义筛选模式
    private enum FilterMode {
        ALL,
        START_END,
        START_ONLY,
        END_ONLY
    }
    // 修改成员变量
    private FilterMode currentFilter = FilterMode.ALL;

    // 甘特图常量
    private static final double BASE_DAY_WIDTH = 40.0;
    private static final double ROW_HEIGHT = 30.0;
    private static final double TIME_AXIS_HEIGHT = 80.0;
    private static final double WEEK_SECTION_HEIGHT = 40.0;

    // 添加currentYearMonth的属性支持
    private final ObjectProperty<YearMonth> currentYearMonthProperty = new SimpleObjectProperty<>(YearMonth.now());

    private YearMonth getCurrentYearMonth() {
        return currentYearMonthProperty.get();
    }

    private ObjectProperty<YearMonth> currentYearMonthProperty() {
        return currentYearMonthProperty;
    }

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();

        loadSchedules();
        setupCalendarListeners();
        refreshAll();

        schedules.addListener((ListChangeListener<? super ScheduleModel>) change -> drawScheduleGantt());

        drawScheduleGantt(); // 初始化时绘制
    }

    void refreshCalendar() {
        calendarGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0
        );

        YearMonth yearMonth = getCurrentYearMonth(); // 使用属性获取当前年月
        monthLabel.setText(yearMonth.getYear() + "年 " +
                yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA));

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int firstDayOfWeek = (firstOfMonth.getDayOfWeek().getValue() + 6) % 7; // 周一为0，周日为6

        LocalDate startDate = firstOfMonth.minusDays(firstDayOfWeek);

        for (int i = 0; i < 6 * 7; i++) {
            LocalDate date = startDate.plusDays(i);

            StackPane dayCell = createDayCell(date);
            int row = (i / 7) + 1;
            int col = i % 7;
            GridPane.setRowIndex(dayCell, row);
            GridPane.setColumnIndex(dayCell, col);
            calendarGrid.getChildren().add(dayCell);
        }
    }



    private void setupCalendarListeners() {
        // 监听当前月份变化时刷新日程显示
        currentYearMonthProperty().addListener((obs, oldVal, newVal) -> {
            refreshAll();
        });

        // 监听日程列表变化（使用单独方法）
        schedules.addListener(this::handleScheduleListChange);
    }


    // 修改原有按钮事件处理逻辑
    @FXML
    private void handlePrevMonth() {
        currentYearMonthProperty.set(getCurrentYearMonth().minusMonths(1));
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonthProperty.set(getCurrentYearMonth().plusMonths(1));
    }



    private void loadSchedules() {
        try {
            // 临时移除监听器避免触发 refreshAll
            schedules.removeListener(this::handleScheduleListChange);
            schedules.setAll(ScheduleDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            schedules.addListener(this::handleScheduleListChange);
        }
    }

    private void handleScheduleListChange(ListChangeListener.Change<? extends ScheduleModel> change) {
        if (!isRefreshing) {
            refreshAll();
        }
    }

    public void refreshAll() {
        if (isRefreshing) return; // 防止重复刷新
        isRefreshing = true;
        loadSchedules();  // 新增：每次刷新前重新加载数据库数据
        refreshCalendar();
        refreshCards();
        isRefreshing = false;
    }

    private void refreshCards() {
        cardsContainer.getChildren().clear();

        List<ScheduleModel> filteredSchedules = schedules.stream()
                .filter(schedule -> {
                    switch (currentFilter) {
                        case START_END:
                            return isDateInThisWeek(schedule.getStartDate()) &&
                                    isDateInThisWeek(schedule.getEndDate());
                        case START_ONLY:
                            return isDateInThisWeek(schedule.getStartDate());
                        case END_ONLY:
                            return isDateInThisWeek(schedule.getEndDate());
                        default:
                            return true; // 显示全部
                    }
                })
                .collect(Collectors.toList());

        filteredSchedules.forEach(schedule -> {
            VBox card = new VBox(5);
            card.getStyleClass().add("schedule-card");
            card.setPrefSize(200, 100);

            // 点击事件（保留原有逻辑）
            card.setOnMouseClicked(event -> {
                cardsContainer.getChildren().forEach(node ->
                        node.getStyleClass().remove("selected-card")
                );
                card.getStyleClass().add("selected-card");
                selectedSchedule = schedule;
            });

            // 卡片内容（保留原有逻辑）
            Label title = new Label(schedule.getTitle());
            title.getStyleClass().add("card-title"); // 添加标题样式类
            Label dates = new Label(schedule.getStartDate() + " - " + schedule.getEndDate());
            dates.getStyleClass().add("card-content"); // 添加内容样式类
            Text content = new Text(schedule.getContent());
            content.setWrappingWidth(180);
            content.getStyleClass().add("card-content"); // 添加内容样式类
            card.getChildren().addAll(title, dates, content);
            cardsContainer.getChildren().add(card);
        });
    }

    private StackPane createDayCell(LocalDate date) {
        Rectangle bg = new Rectangle(40, 40);
        bg.setStroke(Color.LIGHTGRAY);

        // 检查是否是日程结束日期
        boolean isScheduleEnd = schedules.stream()
                .anyMatch(s -> s.getEndDate().equals(date));

        bg.setFill(isScheduleEnd ? Color.rgb(189,208,222) :
                date.getMonth().equals(currentYearMonth.getMonth()) ? Color.WHITE : Color.rgb(237,237,237));

        Text dayText = new Text(String.valueOf(date.getDayOfMonth()));
        return new StackPane(bg, dayText);
    }



    @FXML
    private void handleAddSchedule() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectmanagement/scheduleadd.fxml"));
        Parent root = loader.load();

        ScheduleAddController controller = loader.getController();
        controller.setMainController(this);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("添加日程");
        stage.show();
    }


    // 删除处理方法
    @FXML
    private void handleDeleteSchedule() throws SQLException, IOException {
        if (selectedSchedule == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择要删除的日程").show();
            return;
        }
        // 加载删除确认窗口
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/projectmanagement/scheduledelete.fxml"));
        Parent root = loader.load();
        ScheduleDeleteController deleteController = loader.getController();
        deleteController.setScheduleToDelete(selectedSchedule);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("确认删除");
        stage.initModality(Modality.APPLICATION_MODAL); // 设置为模态窗口
        stage.showAndWait();

        // 如果用户确认删除，则刷新界面
        if (deleteController.isConfirmed()) {
            refreshAll();
            selectedSchedule = null;
        }
    }

    @FXML
    private void handleEditSchedule() throws IOException {
        if (selectedSchedule == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择要编辑的日程").show();
            return;
        }

        // 加载编辑窗口
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectmanagement/scheduleedit.fxml"));
        Parent root = loader.load();

        ScheduleEditController controller = loader.getController();
        controller.setScheduleToEdit(selectedSchedule); // 传递待编辑数据
        controller.setMainController(this);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("编辑日程");
        stage.show();
    }


    @FXML
    private void handleShowDetail() throws IOException {
        if (selectedSchedule == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择要查看的日程").show();
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/projectmanagement/scheduleshow.fxml")
        );
        Parent root = loader.load();

        ScheduleShowController controller = loader.getController();
        controller.setSchedule(selectedSchedule); // 传递选中的日程数据

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("日程详情");
        stage.show();
    }








    @FXML
    private void handleFilterStartEnd() {
        currentFilter = FilterMode.START_END;
        updateFilterButtonText();
        refreshAll();
    }

    @FXML
    private void handleFilterStartOnly() {
        currentFilter = FilterMode.START_ONLY;
        updateFilterButtonText();
        refreshAll();
    }

    @FXML
    private void handleFilterEndOnly() {
        currentFilter = FilterMode.END_ONLY;
        updateFilterButtonText();
        refreshAll();
    }

    @FXML
    private void handleShowAll() {
        currentFilter = FilterMode.ALL;
        updateFilterButtonText();
        refreshAll();
    }

    // 更新按钮文本
    private void updateFilterButtonText() {
        String text = "本周日程";
        switch (currentFilter) {
            case START_END:
                text += " (开始-结束)";
                break;
            case START_ONLY:
                text += " (开始时间)";
                break;
            case END_ONLY:
                text += " (结束时间)";
                break;
        }
        filterMenuButton.setText(text);
    }




    // 新增方法：判断日期是否在本周内（周一至周日）
    private boolean isDateInThisWeek(LocalDate date) {
        LocalDate now = LocalDate.now();
        LocalDate monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return !date.isBefore(monday) && !date.isAfter(sunday);
    }


    // 新增甘特图绘制方法
    private void drawScheduleGantt() {
        if (scheduleGanttCanvas == null || schedules.isEmpty()){
            // 清空画布并直接返回
            GraphicsContext gc = scheduleGanttCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, scheduleGanttCanvas.getWidth(), scheduleGanttCanvas.getHeight());
            return;
        }
        GraphicsContext gc = scheduleGanttCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, scheduleGanttCanvas.getWidth(), scheduleGanttCanvas.getHeight());

//        // 如果没有日程，直接清空画布
//        if (schedules.isEmpty()) {
//            gc.clearRect(0, 0, scheduleGanttCanvas.getWidth(), scheduleGanttCanvas.getHeight());
//            return;
//        }
        // 计算时间范围
        LocalDate minDate = schedules.stream()
                .map(ScheduleModel::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate maxDate = schedules.stream()
                .map(ScheduleModel::getEndDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        // 调整到完整周
        LocalDate adjustedStart = minDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate adjustedEnd = maxDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 计算画布尺寸
        long totalDays = ChronoUnit.DAYS.between(adjustedStart, adjustedEnd) + 1;
        double canvasWidth = 100 + totalDays * BASE_DAY_WIDTH;
        double canvasHeight = TIME_AXIS_HEIGHT + schedules.size() * ROW_HEIGHT + 50;

        scheduleGanttCanvas.setWidth(canvasWidth);
        scheduleGanttCanvas.setHeight(canvasHeight);
        scheduleGanttScrollPane.layout();

        // 绘制时间轴
        drawScheduleTimeAxis(gc, adjustedStart, adjustedEnd, canvasWidth);

        // 绘制日程条
        double yPos = TIME_AXIS_HEIGHT + 20;
        for (ScheduleModel schedule : schedules) {
            long startOffset = ChronoUnit.DAYS.between(adjustedStart, schedule.getStartDate());
            long duration = ChronoUnit.DAYS.between(schedule.getStartDate(), schedule.getEndDate()) + 1;

            double x = 50 + startOffset * BASE_DAY_WIDTH;
            double width = duration * BASE_DAY_WIDTH;

            gc.setFill(Color.rgb(70, 130, 180));
            gc.fillRect(x, yPos, width, 20);

            // 绘制日程标题
            gc.setFill(Color.WHITE);
            gc.fillText(schedule.getTitle(), x + 5, yPos + 15);
            yPos += ROW_HEIGHT;
        }
    }

    // 时间轴绘制方法
    private void drawScheduleTimeAxis(GraphicsContext gc, LocalDate start, LocalDate end, double canvasWidth) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        // 绘制周信息
        LocalDate currentMonday = start;
        int weekNumber = 1;
        while (!currentMonday.isAfter(end)) {

            //当前周一与起始日期之间的天数
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



        // 绘制每日分割线
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

            // 日期标签，使每日日期在时间轴居中的位置
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(currentDay.getDayOfMonth()),
                    xPos + BASE_DAY_WIDTH/2, WEEK_SECTION_HEIGHT + 25);


            //画笔恢复为左对齐，供后续文字使用
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